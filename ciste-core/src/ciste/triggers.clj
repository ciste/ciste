(ns
    ^{:doc "Triggers allow you to have functions called as part of a seperate
thread pool whenever a matching action is invoked.

A Trigger is a function that takes 3 arguments: The action, the
request map, and the response from invoking the action.

All of the dynamic bindings from the original request are coppied to
the trigger.

Example:

    (defaction my-action
      [request]
      {:foo 23, :bar 42})

    (defn my-trigger
      [action request record]
      \"Do something in a different thread\")

    (ciste.trigger/add-trigger! #'my-action #'my-trigger)
"}
    ciste.triggers
  (:use (ciste [config :only [config definitializer describe-config]]
               [debug :only [spy]]))
  (:require (clj-stacktrace [repl :as stacktrace])
            (clojure.tools [logging :as log]))
  (:import java.util.concurrent.Executors
           java.util.concurrent.ExecutorService))

(defonce ^:dynamic *triggers* (ref {}))
(defonce ^:dynamic *thread-pool* (ref nil))

(describe-config [:triggers :thread-count]
  :number
  "The number of executors that should be used to process triggers")

(describe-config [:print :triggers]
  :boolean
  "Log when triggers are executed")

(defn set-thread-pool!
  "Set the number of executors for processing triggers"
  ([]
     (set-thread-pool! (config :triggers :thread-count)))
  ([thread-count]
     (log/debug (str "Setting thread pool to " thread-count))
     (let [pool (Executors/newFixedThreadPool thread-count)]
       (dosync
        (ref-set *thread-pool* pool)))))

(defn- add-trigger*
  [triggers action trigger]
  (assoc triggers action
         (if-let [s (get triggers action)]
           (conj s trigger)
           #{trigger})))

(defn add-trigger!
  "Register a new trigger"
  [action trigger]
  (dosync
   (alter *triggers* add-trigger* action trigger)))

(defn make-trigger
  [trigger action args]
  (let [bindings (get-thread-bindings)]
    (fn []
      (push-thread-bindings bindings)
      (try
        (apply trigger action args)
        (catch Exception e
          (log/error e)
          (stacktrace/pst+ e))
        (finally (pop-thread-bindings))))))

(defn run-triggers
  "Run all triggers for the action"
  [action & args]
  (let [triggers (get @*triggers* action)
        ^ExecutorService pool @*thread-pool*]
    (if pool
      (doseq [trigger triggers]
        (let [^Runnable trigger-fn (make-trigger trigger action args)]
          (when (config :print :triggers)
            (log/info (str "Running " trigger " for " action)))
          (.submit pool trigger-fn)))
      (throw (RuntimeException. "No thread pool defined to handle triggers")))))

(definitializer
  (set-thread-pool!))
