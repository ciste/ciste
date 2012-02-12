(ns
    ^{:author "Daniel E. Renfer <duck@kronkltd.net>"
      :doc "Any fundamental state changes in your application should take place
through an action. Any time you create, update, or delete a resource,
you should use an action. Actions are analogous to the Controller in a
traditional MVC design.

When an action is executed, if the config path [:print :actions] is
enabled, then the action will be logged.

Actions are simply functions. An Action can take any number of
parameters and should return any logically true value if the action
succeeded."
      }
    ciste.core
  (:use (ciste [config :only [config]]))
  (:require (ciste [triggers :as triggers])
            (clojure.tools [logging :as log])
            (lamina [core :as l])))

(defonce ^:dynamic
  ^{:dynamic true
    :doc "The current format in use. Rebind this var to set the format for the
          current request."}
  *format* nil)
(defonce ^:dynamic *serialization* nil)
(defonce ^:dynamic *actions* (l/permanent-channel))
(l/receive-all *actions* (fn [_]))

(defmacro with-serialization
  "Set the bindings for the serialization."
  [serialization & body]
  `(binding [*serialization* ~serialization]
     ~@body))

(defmacro with-format
  "Set the bindings for the format"
  [format & body]
  `(binding [*format* ~format]
     ~@body))

(defmacro with-context
  "Set the bindings for both the serialization and the format"
  [[serialization format] & body]
  `(with-serialization ~serialization
    (with-format ~format
      ~@body)))

(defmacro defaction
  "Define an Action.

   An Action is similar to a ordinary function except that it announces itself
   to the action channel, it logs it's execution and it executes any associated
   triggers.

   Config options used:

   * [:print :actions] - If true, this Action will log itself on every execution
   * [:use-pipeline] - If true, the result of executing this action will be
     enqueued to the action channel.
   * [:run-triggers] - If true, associated triggers will be run after this
     action executes."
  [name & forms]
  (let [[docs forms] (if (string? (first forms))
                         [(first forms) (rest forms)]
                         ["" forms])
        [args & forms] forms]
    `(do
       (defn ~name
         ~docs
         [& params#]
         (let [~args params#
               action# (var ~name)]
           (when (config :print :actions) (log/info action#))
           (let [records# (do ~@forms)]
             ;; TODO: Find a good way to hook these kind of things
             (when (config :use-pipeline)
               (l/enqueue *actions* {:action action# :args params# :records records#}))
             (when (config :run-triggers)
               (triggers/run-triggers action# params# records#))
             records#)))
       (alter-meta! (var ~name) assoc :arglists '(~args))
       (var ~name))))

(defmulti serialize-as
  "Format the response based on it's serialization type"
  (fn [x & _] x))

(defmulti apply-template
  "Attach a template based on the current format of the request."
  (fn [request response] (:format request)))

;; By default, no template is defined. Override this method to include
;; a custom template
(defmethod apply-template :default
  [request response]
  response)
