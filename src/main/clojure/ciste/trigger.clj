(ns ciste.trigger
  (:import java.util.concurrent.Executors))

(def #^:dynamic *triggers* (ref {}))
(def #^:dynamic *thread-count* 2)
(def #^:dynamic *thread-pool*
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
          (.printStackTrace e))
        (finally (pop-thread-bindings))))))

(defn run-triggers
  [action & args]
  (let [triggers (get @*triggers* action)]
    (doseq [trigger triggers]
      (let [trigger-fn (make-trigger trigger action args)]
        (println "Running" trigger "for" action)
        (.submit @*thread-pool* trigger-fn)))))

(defn sleep-and-print
  [& args]
  (Thread/sleep 3000)
  ;; (println "foo")
  )
