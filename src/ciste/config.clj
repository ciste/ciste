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
  (load-config)
  (set-environment! :default)
  (config :option1) => \"foo\"
  (config :option3) => [\"foo\" \"bar\" \"baz\"]
  (config :option2 :title) => \"BAR\""
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.executor :refer [task]]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import java.io.FileNotFoundException
           java.net.InetAddress))

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

(defonce
  ^{:doc "By default, the runner will look for a file with this name at the root
          of the project directory."}
  default-site-config-filename "ciste.clj")

(defonce
  ^{:doc "Ref containing the currently loaded site config"}
  default-site-config (ref {}))

(defn get-host-name
  "Returns the hostname of the host's local adapter."
  []
  (.getHostName (InetAddress/getLocalHost)))

(defn get-host-address
  "Returns the IP address of the host's local adapter"
  []
  (.getHostAddress (InetAddress/getLocalHost)))

(defn environment*
  []
  @*environment*)

(defn environment
  "Returns the currently bound environment.

  Throws an exception if no environment is bound"
  []
  (or (environment*)
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

(defn get-resource
  [filename]
  (some-> (or (let [f (io/file filename)]
                (when (.exists f) f))
              (io/resource filename))
          slurp
          ;; TODO: Use EDN reader
          read-string))

(defn load-config
  "Loads the config file into the environment.

  Defaults to config.clj if not specified"
  ([] (load-config "config.clj"))
  ([filename]
   (when-let [options (get-resource filename)]
     (dosync
      (ref-set *environments* options)))))

(defn read-site-config
  "Read the site config file"
  ([] (read-site-config default-site-config-filename))
  ([filename]
   (or (get-resource filename)
       (throw+ "Could not find service config."))))

;; TODO: This should attempt to write the config back to the same
;; place it was loaded from.
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
         (log/debug (format "%-35s => %-20s %s" config-part (pr-str response) default-part)))
     response)))

(defn config
  "Returns the option matching the key sequence in the global config map for the
   currently bound environment, defaulting to the :default environment.

   Throws an exception if the option can not be found"
  ([& ks]
   (let [value (apply config* ks)]
     (if-not (nil? value)
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

(defn set-environment!
  "Sets's the environment globally"
  [env]
  (log/debugf "Setting environment to %s" env)
  (dosync (reset! *environment* env)))

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

(defn config-doc
  "Print out the documentation for the config path"
  [& ks]
  (when-let [m (get @*doc-maps* (vec ks))]
    (println (:path m))
    (println " " (:type m))
    (println (:doc m))))

(defn load-site-config
  "Read the site config and store it for later use"
  []
  (let [site-config (read-site-config)]
    (dosync
     (ref-set default-site-config site-config))))

