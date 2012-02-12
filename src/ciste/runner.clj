(ns ciste.runner
  (:use (ciste [config :only [load-config set-environment!]]
               [debug :only [spy]]))
  (:require (clojure.tools [logging :as log]))
  (:import java.io.FileNotFoundException))

(defn read-site-config
  []
  ;; TODO: Check a variety of places for this file.
  (-> "ciste.clj" slurp read-string))

(defn -main
  [& options]
  (let [opts (apply hash-map options)]
    (try
      (let [service-config (read-site-config)

            ;; TODO: allow this to be passed in via command line
            environment (:environment service-config)]

        ;; TODO: initialize config backend
        (load-config)

        ;; TODO: load namespaces other than services to be started
        (doseq [sn (:services service-config)]
          (log/debug (str "Loading " sn))
          (require (symbol sn)))
        
        (log/info (str "Starting service in " environment " mode."))
        (set-environment! environment)
        
        (doseq [service-name (:services service-config)]
          (log/info (str "Starting " service-name))
          ((intern (the-ns (symbol service-name)) (symbol "start"))))
        
        ;; TODO: store this and allow it for shutdown.
        @(promise))
      (catch FileNotFoundException ex
        (log/error "Could not find service config. Ensure that ciste.clj exists at the root of your application and is readable")
        (System/exit -1)))))
