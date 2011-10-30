(ns ciste.core
  (:use (ciste [config :only [config]]))
  (:require (ciste [triggers :as triggers])
            (clojure.tools [logging :as log])
            (lamina [core :as l])))

(defonce ^:dynamic *format* nil)
(defonce ^:dynamic *serialization* nil)
(defonce ^:dynamic *actions* (l/permanent-channel))
(l/receive-all *actions* (fn [_]))

(defmacro with-serialization
  [serialization & body]
  `(binding [*serialization* ~serialization]
     ~@body))

(defmacro with-format
  [format & body]
  `(binding [*format* ~format]
     ~@body))

(defmacro with-context
  [[serialization format] & body]
  `(with-serialization ~serialization
    (with-format ~format
      ~@body)))

(defmacro defaction
  [name args & forms]
  `(defn ~name
     [& params#]
     (let [~args params#
           action# (var ~name)]
       (when (config :print :action) (log/info (str action#)))
       (let [records# (do ~@forms)]
         ;; TODO: Find a good way to hook these kind of things
         (when (config :use-pipeline)
           (l/enqueue *actions* {:action action# :args params# :records records#}))
         (when (config :run-triggers)
           (triggers/run-triggers action# params# records#))
         records#))))

(defmulti serialize-as (fn [x & _] x))

(defmulti apply-template (fn [request response] (:format request)))

(defmethod apply-template :default
  [request response]
  response)
