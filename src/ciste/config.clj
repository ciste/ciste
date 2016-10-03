(ns ciste.config
  "Ciste uses the config function in ciste.config to perform all the
  configuration. Config takes a variable number of key values and will
  either return a non-nil value if that option is defined, or will raise
  an exception if it is not.

  The config information is read from the file \"config.clj\" at the base
  of the project's directory. The config file should contain a hash-map.

  The top-level keys will be the names of environments. The values of
  these keys will be an arbitrarily complex structure of hashes,
  vectors, and other data.

  Example:

  (use 'ciste.config)
  (load-config!)
  (set-environment! :default)
  (config :option1) => \"foo\"
  (config :option3) => [\"foo\" \"bar\" \"baz\"]
  (config :option2 :title) => \"BAR\""
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]
            [clojurewerkz.propertied.properties :as p]
            [environ.core :refer [env]]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import java.net.InetAddress))

(defonce
  ^{:dynamic true
    :doc "The full config map for all environments is stored
          in this ref"}
  *config-map* (ref {}))

(defonce
  ^{:dynamic true
    :doc "This is where config docs are kept"}
  *doc-maps*
  (ref {}))

(defn get-host-address
  "Returns the IP address of the host's local adapter"
  []
  (.getHostAddress (InetAddress/getLocalHost)))

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

(defn get-resource
  [filename]
  (or (let [f (io/file filename)]
        (when (.exists f) f))
      (io/resource filename)))

(defn load-config!
  "Loads the config file into the environment."
  ([] (load-config!"config.properties"))
  ([filename]
   (timbre/infof "Loading config file: %s" filename)
   (when-let [file (get-resource filename)]
     (let [options (p/load-from file)]
       (dosync
        (ref-set *config-map* options))))))

;; TODO: This should attempt to write the config back to the same
;; place it was loaded from.
(defn write-config!
  "Write the current config settings to file"
  ([] (write-config! "config.clj"))
  ([filename]
   (->> @*config-map*
        clojure.pprint/pprint
        with-out-str
        (spit filename))))

(defn config*
  "Like config, but does not throw an exception if the key cannot be found."
  [& ks]
  (let [path-strings (map name ks)
        dot-path (string/join "." path-strings)
        dash-path (string/join "-" path-strings)]
    (or (env (keyword dash-path))
        (get @*config-map* dot-path)
        (some-> @*doc-maps* (get (vec ks)) :default))))

(defn config
  "Returns the option matching the key sequence in the global config map for the
   currently bound environment, defaulting to the :default environment.

   Throws an exception if the option can not be found"
  ([& ks]
   (let [value (apply config* ks)]
     (if-not (nil? value)
       value
       (throw (IllegalArgumentException. (str "no config option matching path " ks)))))))

(defmacro describe-config
  "Macro to record config information

   Example:

       (describe-config [:print :request]
         :boolean
         \"Should the request be logged?\")"
  [ks datatype docstring & {:as body}]
  (let [m (merge {:path ks
                  :doc docstring
                  :type datatype}
                 body)]
    `(dosync
      (alter *doc-maps* assoc ~ks ~m))))

(defn config-doc
  "Print out the documentation for the config path"
  [& ks]
  (when-let [m (get @*doc-maps* (vec ks))]
    (println (:path m))
    (println " " (:type m))
    (println (:doc m))))
