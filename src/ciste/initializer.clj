(ns ciste.initializer
  (:use [ciste.config :only [environment]]
        [lamina.executor :only [task]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]))


(defonce ^:dynamic *initializers* (ref []))
(defonce ^:dynamic *initializer-order* (ref []))


(defmacro definitializer
  "Defines an initializer. When an environment is bound, the initializers will
be run in the order that they are loaded.

Initializers are blocks of code that need to set up the environment of
the namespace, but cannot run until the configuration system is
available with a valid environment.

Whenever the environment is changed, the initializers will run in the
order they were declared.

Note: At this time, Initializers will be re-run if the namespace is
reloaded. For this reason, it is recommended that initializers be able
to handle being run multiple times gracfully.

Example:

    (ns ciste.example
      (:use [ciste.config :only (definitializer)]))

    (definitializer
      (println \"This will be run when the environment is set\")
      (println (config :hostname)))

    (println \"out of the initializer\"


    > (use 'ciste.example)
    out of the initializer
    > (set-environment! :development)
    This will be run when the environment is set
    server1.example.com"
  [& body]
  `(let [init-fn# (fn []
                    #_(log/debug (str "running initializer - " *ns*))
                    ~@body)]
     (dosync
      #_(log/debug (str "adding initializer - " *ns*))
      (alter *initializers* conj init-fn#))
     (try
       (when (environment) (init-fn#))
       (catch RuntimeException e#))))

(defn run-initializers!
  "Run all initializers"
  []
  (task
   (doseq [init-fn @*initializers*]
     (init-fn))))

