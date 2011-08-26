(ns ciste.triggers
  (:use ciste.config
        [clojure.tools.logging :only (info)])
  (:require [clojure.stacktrace :as stacktrace])
  (:import java.util.concurrent.Executors))

(defonce ^:dynamic *triggers* (ref {}))
(defonce ^:dynamic *thread-count* (or (-> (config) :triggers :thread-count)
                                      10))
(defonce ^:dynamic *thread-pool*
  (ref (Executors/newFixedThreadPool *thread-count*)))

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
          (println (stacktrace/print-stack-trace e)))
        (finally (pop-thread-bindings))))))

(defn run-triggers
  [action & args]
  (let [triggers (get @*triggers* action)]
    (doseq [trigger triggers]
      (let [trigger-fn (make-trigger trigger action args)]
        (info (str "Running " trigger " for " action))
        (.submit @*thread-pool* trigger-fn)))))

(defn sleep-and-print
  [& args]
  (Thread/sleep 3000)
  ;; (println "foo")
  )
