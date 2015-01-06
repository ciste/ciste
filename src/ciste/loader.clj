(ns ciste.loader
  (:use [ciste.config :only [config config* default-site-config load-config
                             set-environment!]])
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]])
  (:import java.util.concurrent.ConcurrentLinkedQueue))

(defonce pending-requires (ConcurrentLinkedQueue.))

(defn- root-resource
  "Returns the root directory path for a lib"
  {:tag String}
  [lib]
  (str
   (.. (name lib)
       (replace \- \_)
       (replace \. \/))))

(defn consume-require
  [sym]
  (try+
    (let [file-name (str (root-resource sym) ".clj")]
      (if true #_(io/resource file-name)
        (do
          (log/debugf "Loading %s" sym)
          (require sym))
        (log/warnf "Could not find: %s" sym)))
    (catch java.io.FileNotFoundException ex
      (log/debugf "can't find file: %s" sym))
    (catch Throwable ex
      #_(trace/trace :errors:handled ex)
      (.printStackTrace ex)
      (System/exit -1))))


(defn require-namespaces
  "Require the sequence of namespace strings"
  [namespaces]
  (log/debug "requiring namespaces")
  (doseq [sn namespaces]
    (let [sym (symbol sn)]
      (log/debugf "Adding Module: %s" sym)
      (.add pending-requires sym))))

(defn require-modules
  "Require each namespace"
  ([] (require-modules @default-site-config))
  ([service-config]
     (require-namespaces (concat (:modules service-config)
                                 (:services service-config)
                                 (config* :modules)
                                 (config* :services)))))

(defn process-requires
  []
  (loop [sym (.poll pending-requires)]
    (when sym
      (consume-require sym)
      (recur (.poll pending-requires))))
  (log/info "Done processing requires"))
