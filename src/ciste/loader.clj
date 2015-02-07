(ns ciste.loader
  (:require [ciste.config :refer [config config* default-site-config load-config
                                 set-environment!]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [try+]])
  (:import java.util.concurrent.ConcurrentLinkedQueue))

(defonce pending-requires (ConcurrentLinkedQueue.))
(def modules (ref {}))
(def handlers (ref {}))

(defn defhandler
  [name doc channel handler]
  (dosync
   (alter handlers assoc name {:channel channel
                               :handler handler})))

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
      (if (io/resource file-name)
        (do
          ;; (log/debugf "Loading %s" sym)
          (require sym)
          )
        #_(log/warnf "Could not find: %s" sym)))
    (catch java.io.FileNotFoundException ex
      (log/debugf "can't find file: %s" sym))
    (catch Throwable ex
      (log/error "Could not consume require" ex)
      (System/exit -1))))

(defn require-namespaces
  "Require the sequence of namespace strings"
  [namespaces]
  (doseq [sn namespaces]
    (let [sym (symbol sn)]
      ;; (log/debugf "Adding NS: %s" sym)
      (.add pending-requires sym))))

(defn register-module
  [name]
  (log/infof "Registering module: %s" name)
  (let [sym (symbol name)]
    (require sym)
    (try
      (when-let [start-fn (ns-resolve sym 'start)]
        (log/infof "Starting Module: %s" name)
        (start-fn))
      (catch Exception ex
        (log/error "failed to start" ex)))))

(defn define-module
  [name options]
  (dosync
   (alter modules assoc name options)))

(defn defmodule
  [name & {:as options}]
  (define-module name options))

(defn require-modules
  "Require each namespace"
  ([] (require-modules @default-site-config))
  ([service-config]
     (let [modules (concat (:modules service-config)
                           (:services service-config)
                           (config* :modules)
                           (config* :services))]
       (doseq [module modules]
         (register-module module)))))

(defn process-requires
  []
  (loop [sym (.poll pending-requires)]
    (when sym
      (consume-require sym)
      (recur (.poll pending-requires))))
  (log/info "Done processing requires"))
