(ns ciste.loader
  (:require [ciste.config :refer [config*]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre])
  (:import java.io.FileNotFoundException
           java.util.concurrent.ConcurrentLinkedQueue))

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
         ;; (timbre/debugf "Loading %s" sym)
         (require sym))
       #_(timbre/warnf "Could not find: %s" sym)))
   (catch FileNotFoundException ex
     (timbre/debugf ex "can't find file: %s" sym))
   (catch Throwable ex
     (timbre/error ex "Could not consume require")
     (System/exit -1))))

(defn require-namespaces
  "Require the sequence of namespace strings"
  [namespaces]
  (doseq [sn namespaces]
    (let [sym (symbol sn)]
      ;; (timbre/debugf "Adding NS: %s" sym)
      (.add pending-requires sym))))

(defn register-module
  [module-name]
  ;; (timbre/debugf "Registering module: %s" module-name)
  (try
    (let [sym (symbol module-name)]
      (require sym)
      (if-let [start-fn (ns-resolve sym 'start)]
        (do
          (timbre/infof "Starting Module: %s" module-name)
          (start-fn))
        (do
          (timbre/debug "Module does not provide a start function")
          nil)))
    (catch Exception ex
      (timbre/error ex "failed to start")
      (System/exit -1)
      (throw+ "Module load failure" ex))))

(defn define-module
  [module-name options]
  ;; (timbre/debugf "defining module: %s" module-name)
  (dosync
   (alter modules assoc module-name options)))

(defn defmodule
  [name & {:as options}]
  (define-module name options))

(defn get-modules
  "Return the sequence of registered modules"
  []
  (some->>
   (some-> (config* :modules)
           (string/split #","))
   (remove empty?)))

(defn require-modules
  "Require each namespace"
  ([] (require-modules nil))
  ([module-args]
   (let [resolved-modules (or module-args (get-modules))]
     (if-not (empty? resolved-modules)
       (doseq [module resolved-modules]
         (if (@modules module)
           (timbre/debug "Module already loaded")
           (register-module module)))
       (timbre/warn "No modules specified")))))

(defn process-requires
  []
  (loop [sym (.poll pending-requires)]
    (when sym
      (consume-require sym)
      (recur (.poll pending-requires))))
  (timbre/info "Done processing requires"))
