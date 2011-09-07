(ns ciste.triggers
  (:use ciste.config)
  (:require (clojure [stacktrace :as stacktrace])
            (clojure.tools [logging :as log]))
  (:import java.util.concurrent.Executors))

(defonce ^:dynamic *triggers* (ref {}))
(defonce ^:dynamic *thread-pool* (ref nil))

(defn set-thread-pool!
  ([]
     (set-thread-pool! (config :triggers :thread-count)))
  ([thread-count]
     (let [pool (Executors/newFixedThreadPool thread-count)]
       (dosync
        (ref-set *thread-pool* pool)))))

(definitializer
  (set-thread-pool!))

(defn- add-trigger*
  [triggers action trigger]
  (assoc triggers action
         (if-let [s (get triggers action)]
           (conj s trigger)
           #{trigger})))

(defn add-trigger!
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
          (log/error e))
        (finally (pop-thread-bindings))))))

(defn run-triggers
  [action & args]
  (let [triggers (get @*triggers* action)
        pool @*thread-pool*]
    (if pool
      (doseq [trigger triggers]
        (let [trigger-fn (make-trigger trigger action args)]
          (if (config :print :triggers)
            (log/info (str "Running " trigger " for " action)))
          (.submit pool trigger-fn)))
      (throw (RuntimeException. "No thread pool defined to handle triggers")))))
