(ns ciste.loader
  (:use [ciste.config :only [config default-site-config load-config set-environment!]])
  (:require [clojure.tools.logging :as log])
  (:import java.util.concurrent.ConcurrentLinkedQueue))

(defonce pending-requires (ConcurrentLinkedQueue.))

(defn consume-require
  [sym]
  (try
    (log/debugf "Loading %s" sym)
    (require sym)
    (catch Exception ex
      (log/error ex)
      (.printStackTrace ex)
      (System/exit 0))))


(defn require-namespaces
  "Require the sequence of namespace strings"
  [namespaces]
  (doseq [sn namespaces]
    (let [sym (symbol sn)]
      (.add pending-requires sym))))

(defn require-modules
  "Require each namespace"
  ([] (require-modules @default-site-config))
  ([service-config]
     (require-namespaces (concat (:modules service-config)
                                 (:services service-config)
                                 (config :modules)
                                 (config :services)))))

(defn process-requires
  []
  (loop [sym (.poll pending-requires)]
    (if sym
      (do (consume-require sym)
          (recur (.poll pending-requires)))
      (log/info "Done processing requires"))))
