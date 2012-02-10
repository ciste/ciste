(ns
    ^{:author "Daniel E. Renfer <duck@kronkltd.net>"}
    ciste.config
  (:use (ciste [debug :only [spy]]))
  (:require (clojure [string :as string])
            (clojure.tools [logging :as log]))
  (:import java.net.InetAddress))

;; TODO: read from env var
(defonce
  ^{:dynamic true
    :doc "The current environment. use the set-environment!,
          environment, and with"}
  *environment* (atom nil))

(defonce
  ^{:dynamic true
    :doc "The full config map for all environments is stored
          in this ref"}
  *environments* (ref {}))

(defonce
  ^{:dynamic true
    :doc "This is where config docs are kept"}
  *doc-maps*
  (ref {}))


(defonce ^:dynamic *initializers* (ref []))
(defonce ^:dynamic *initializer-order* (ref []))


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
  (->> (map (fn [[k v]]
              [k (if (map? v)
                   (merge-config v (get m2 k))
                   (let [m2-val (get m2 k)]
                     (or m2-val v)))])
            m1)
       (into {})
       (merge m2)))

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

(defn write-config!
  "Write the current config settings to file"
  ([] (write-config! "config.clj"))
  ([filename]
     (->> @*environments*
          clojure.pprint/pprint
          with-out-str
          (spit filename))))

(defn config*
  "Like config, but does not throw an exception if the key cannot be found."
  ([]
     (get @*environments* (environment)))
  ([& ks]
     (let [env-val (get-in (config*) ks)
           default-val (get-in (:default @*environments*) ks)
           value (if (nil? env-val)
                   default-val
                   env-val)
           response (if (map? env-val)
                      (merge-config env-val default-val)
                      value)]
       #_(let [config-part (str "(config " (string/join " " ks) ")")
               default-part (if (= response default-val) ":default" "")]
           (log/debug (format "%-35s => %-20s %s" config-part val default-part)))
       response)))

(defn config
  "Returns the option matching the key sequence in the global config map for the
   currently bound environment, defaulting to the :default environment.

   Throws an exception if the option can not be found"
  ([& ks]
     (let [value (apply config* ks)]
       (if (not (nil? value))
         value
         (throw
          (IllegalArgumentException.
           (str "no config option matching path " ks " for " (environment))))))))

(defn set-config!
  "Set the value of the config setting matching the key sequence"
  [ks value]
  (dosync
   (alter *environments*
          assoc-in (concat [(environment)] ks) value))
  value)

(defmacro definitializer
  "Defines an initializer. When an environment is bound, the initializers will
   be run in the order that they are loaded."
  [& body]
  `(let [init-fn# (fn [] ~@body)]
     (dosync
      (alter *initializers* conj init-fn#))
     (try
       (when (environment) (init-fn#))
       (catch RuntimeException e#))))

(defn run-initializers!
  "Run all initializers"
  []
  (doseq [init-fn @*initializers*]
    (init-fn)))

(defn set-environment!
  "Sets's the environment globally"
  [env]
  (dosync
   (reset! *environment* env))
  (run-initializers!))

(defmacro with-environment
  "Run body with the evironment bound"
  [environment & body]
  `(binding [ciste.config/*environment* (atom nil)]
     (set-environment! ~environment)
     ~@body))

(defmacro describe-config
  "Macro to record config information

   Example:

       (describe-config [:print :request]
         :boolean
         \"Should the request be logged?\")"
  [ks type docstring & body]
  `(dosync
    (alter *doc-maps*
           assoc ~ks {:path ~ks
                      :doc ~docstring
                      :type ~type})))

(defn doc
  "Print out the documentation for the config path"
  [& ks]
  (when-let [config-doc (get @*doc-maps* (vec ks))]
    (println (:path config-doc))
    (println " " (:type config-doc))
    (println (:doc config-doc))))
