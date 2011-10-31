(ns ciste.workers
  (:use (ciste [debug :only [spy]])
        (clj-factory [core :only [fseq defseq]])
        (clj-stacktrace [repl :only [pst+]])
        (clojure.core [incubator :only (dissoc-in)]))
  (:require (ciste [config :as config]
                   [triggers :as triggers])
            (clojure [string :as string])
            (clojure.tools [logging :as log])))

(defonce ^:dynamic *workers* (ref {}))
(defonce ^:dynamic *current-name* nil)
(defonce ^:dynamic *current-id* nil)


(defseq :id [n] n)

(defmulti execute-worker (fn [x & _] x))

(defn current-name
  []
  (or *current-name*
      (throw
       (RuntimeException. "No name defined"))))

(defn current-id
  []
  (or *current-id*
      (throw
       (RuntimeException. "No id defined"))))

(defn stopping?
  ([]
     (stopping? (current-name) (current-id)))
  ([name id]
     (-> @*workers*
         (get name)
         (get id)
         :stopping)))

(defn worker-keys
  "Returns a sequence of registered worker types"
  []
  (keys (methods execute-worker)))

(defn make-worker-fn
  [name id inner-fn]
  (fn worker-fn [name & args]
    (future
      (try
        (binding [*current-id* id
                  *current-name* name]
          (loop []
            (try
              (apply inner-fn name args)
              (catch Exception e
                (log/error e "Uncaught exception")
                (Thread/sleep 4000)))
            (let [stopping (stopping? name id)]
              (log/debug (str "(stopping? "
                              name " " id "): " stopping))
              (if-not stopping (recur)))))
        (catch Exception e
          (pst+ e))
        (finally
         (log/info (str "Worker " name " (" id ") finished"))
         (dosync
          (alter *workers* dissoc-in [name id])))))))

(defn start-worker*
  [name id worker-fn args]
  (log/info (str "Starting worker " name
                 "(" (string/join " " args) ") => " id))
  (let [inst (apply worker-fn name args)
        m {:worker inst
           :host (config/get-host-name)
           :counter 0
           :stopping false
           :id id}]
    (dosync (alter *workers* assoc-in [name id] m))
    m))

(defn increment-counter!
  ([] (increment-counter! 1))
  ([n] (increment-counter! (current-name) (current-id) n))
  ([name id n]
     (dosync
      (alter *workers*
             (fn [w]
               (let [counter (get-in w [name id :counter])]
                 (assoc-in w [name id :counter] (+ counter n))))))))

(defmacro defworker
  [name args & body]
  `(let [name# ~name]
     (defmethod execute-worker name#
       [worker-name# & args#]
       (let [id# (fseq :id)
             inner-fn# (fn inner-fn [worker-name# & ~args] ~@body)
             worker-fn# (make-worker-fn name# id# inner-fn#)]
         (start-worker* name# id# worker-fn# args#)))))

(defn start-worker!
  [worker-name & args]
  (apply execute-worker worker-name args))

(defn stop-worker!
  [name id]
  (dosync
   (alter *workers* assoc-in [name id :stopping] true)))

(defn stop-all-workers!
  []
  (log/info "Stopping all workers")
  (doseq [[name data] @*workers*]
    (doseq [[id _] data]
      (stop-worker! name id))))
