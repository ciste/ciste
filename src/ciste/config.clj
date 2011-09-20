(ns ciste.config
  (:require (clojure.tools [logging :as log]))
  (:import java.net.InetAddress))

;; TODO: read from env var
(defonce ^:dynamic *environment* (atom nil))
(defonce ^:dynamic *environments* (ref {}))
(defonce ^:dynamic *initializers* (ref []))

(defn get-host-name
  []
  (.getHostName (InetAddress/getLocalHost)))

(defn get-host-address
  []
  (.getHostAddress (InetAddress/getLocalHost)))

(defn environment
  []
  (or @*environment*
      (throw
       (RuntimeException.
        "Environment not set. export CISTE_ENV to choose an environment"))))

(defn config
  ([]
     (get @*environments* (environment)))
  ([& ks]
     (let [val (or (get-in (config) ks)
                   (get-in (get @*environments* :default) ks))]
       (if (nil? val)
         (throw (IllegalArgumentException.
                 (str "no config option matching path " ks
                      " for " (environment))))
         val))))

(defn load-config
  ([] (load-config "config.clj"))
  ([filename]
     (->> filename
          slurp
          read-string
          (ref-set *environments*)
          dosync)))

(defn add-option!
  [option value]
  ;; TODO: implement
  )

(defmacro definitializer
  [& body]
  `(let [namespace# *ns*
         init-fn# (fn []
                    (log/info (str "running initializer for " namespace# ))
                    ~@body)]
     (log/info (str "Adding initializer for " namespace#))
     (dosync
      (alter *initializers* conj init-fn#))
     (try
       (if (environment) (init-fn#))
       (catch RuntimeException e#))))

(defn initialize
  []
  (doseq [initializer @*initializers*]
    (initializer)))

(defn set-environment!
  [env]
  (dosync
   (reset! *environment* env))
  (initialize))

(defmacro with-environment
  [environment & body]
  `(do (load-config)
       (binding [ciste.config/*environment* (atom nil)]
         (set-environment! ~environment)
         ~@body)))
