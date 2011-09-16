(ns ciste.workers
  (:use (ciste [debug :only (spy)])
        (clojure [stacktrace :only (print-stack-trace)])
        (clj-factory [core :only (fseq defseq)]))
  (:require (ciste [config :as config]
                   [triggers :as triggers])
            (clojure [string :as string])
            (clojure.contrib [core :as c])
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
          (print-stack-trace e))
        (finally
         (log/info (str "Worker " name " (" id ") finished"))
         (dosync
          (alter *workers* c/dissoc-in [name id])))))))

(defn start-worker*
  [name id worker-fn args]
  (log/info (str "Starting worker " name
                 "(" (string/join " " args) ") => " id))
  (let [inst (apply worker-fn name args)
        m {:worker inst
           :host (config/get-host-name)
           :stopping false
           :id id}]
    (dosync (alter *workers* assoc-in [name id] m))
    m))

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
