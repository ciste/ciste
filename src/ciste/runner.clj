(ns ciste.runner
  (:use (ciste [config :only [load-config set-environment!]]
               [debug :only [spy]]))
  (:require (clojure.tools [logging :as log])))

(defn read-site-config
  []
  (-> "ciste.clj" slurp read-string))

(defn -main
  [& options]
  (let [opts (apply hash-map options)
        environment :development
        service-config (read-site-config)]
    (load-config)

    (doseq [sn (:services service-config)]
      (require (symbol sn)))

    (log/info (str "Starting service in " environment " mode."))

    ;; TODO: Also read environment variable
    (set-environment! (:environment service-config))

    (doseq [service-name (:services service-config)]
      (log/info (str "Loading " service-name))
      ((intern (the-ns (symbol service-name)) (symbol "start"))))

    ;; TODO: store this and allow it for shutdown.
    @(promise)))
