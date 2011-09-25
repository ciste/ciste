(ns ciste.config
  (:use (ciste [debug :only (spy)]))
  (:require (clojure.tools [logging :as log]))
  (:import java.net.InetAddress))

;; TODO: read from env var
(defonce ^:dynamic *environment* (atom nil))
(defonce ^:dynamic *environments* (ref {}))
(defonce ^:dynamic *initializers* (ref []))

(defn get-host-name
  "Returns the hostname of the host's local adapter."
  []
  (.getHostName (InetAddress/getLocalHost)))

(defn get-host-address
  "Returns the IP address of the host's local adapter"
  []
  (.getHostAddress (InetAddress/getLocalHost)))

(defn environment
  "Returns the currently bound environment.

Throws an exception if no environment is bound"
  []
  (or @*environment*
      (throw
       (RuntimeException.
        "Environment not set. export CISTE_ENV to choose an environment"))))

(defn merge-config
  "Recursively merges m1 into m2. If the value of any of the key is a map, the
elements in that map are also merged"
  [m1 m2]
  (->> (map
        (fn [[k v]]
          [k (if (map? v)
               (merge-config v (get m2 k))
               (let [m2-val (get m2 k)]
                 (or m2-val v)))])
        m1)
       (into {})
       (merge m2)))


(defn config
  "Returns the option matching the key sequence in the global config map for the
currently bound environment, defaulting to the :default environment.

Throws an exception if the option can not be found"
  ([]
     (get @*environments* (environment)))
  ([& ks]
     (let [env-val (get-in (config) ks)
           default-val (get-in (get @*environments* :default) ks)
           val (if (nil? env-val) default-val env-val )]
       (if (map? env-val)
         (merge-config env-val default-val)
         (if (not (nil? val))
           val
           (throw (IllegalArgumentException.
                   (str "no config option matching path " ks
                        " for " (environment)))))))))

(defn load-config
  "Loads the config file into the environment.

Defaults to config.clj if not specified"
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
  "Defines an initializer. When an environment is bound, the initializers will
be run in the order that they are loaded."
  [& body]
  `(let [namespace# *ns*
         init-fn# (fn []
                    #_(log/info (str "running initializer for " namespace# ))
                    ~@body)]
     #_(log/info (str "Adding initializer for " namespace#))
     (dosync
      (alter *initializers* conj init-fn#))
     (try
       (if (environment) (init-fn#))
       (catch RuntimeException e#))))

(defn initialize
  "Run all initializers"
  []
  (doseq [initializer @*initializers*]
    (initializer)))

(defn set-environment!
  "Sets's the environment globally"
  [env]
  (dosync
   (reset! *environment* env))
  (initialize))

(defmacro with-environment
  "Run body with the evironment bound"
  [environment & body]
  `(do (load-config)
       (binding [ciste.config/*environment* (atom nil)]
         (set-environment! ~environment)
         ~@body)))
