(ns ciste.service
  (:use [ciste.config :only [config load-config set-environment!
                             default-site-config]]
        [ciste.initializer :only [run-initializers!]]
        [ciste.loader :only [process-requires require-modules]])
  (:require [clojure.tools.logging :as log]))

(defn start-services!
  "Start each service."
  ([] (start-services! @default-site-config))
  ([site-config]
     (doseq [service-name (concat (:services site-config)
                                  (config :services))]
       (let [service-sym (symbol service-name)]
         (log/info (str "Starting " service-name))
         (require service-sym)
         ((intern (the-ns service-sym) (symbol "start")))))))

(defn init-services
  "Ensure that all namespaces for services have been required and that the
   config provider has benn initialized"
  [environment]
  ;; TODO: initialize config backend
  (load-config)
  (set-environment! environment)
  (require-modules)
  (run-initializers!)
  (process-requires))

(defn stop-services!
  ([] (stop-services! @default-site-config))
  ([site-config]
     (doseq [service-name (concat (:services site-config)
                                  (config :services))]
       (log/info (str "Stopping " service-name))
       ((intern (the-ns (symbol service-name)) (symbol "stop"))))))

