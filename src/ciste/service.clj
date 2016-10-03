(ns ciste.service
  (:require [ciste.config :refer [config config* load-config!]]
            [ciste.loader :as loader]
            [environ.core :refer [env]]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

(defn init-services
  "Ensure that all namespaces for services have been required and that the
   config provider has benn initialized"
  [modules]
  ;; TODO: initialize config backend
  (load-config! (env :ciste-properties "config/ciste.properties"))
  (loader/require-modules modules)
  (loader/process-requires))

(defn stop-services!
  "Shut down all services"
  []
  ;; (timbre/debug "stopping services")
  (doseq [module-name (keys @loader/modules)]
    (if-let [module-ns (some-> module-name symbol the-ns)]
      (do
        (timbre/debugf "Stopping service - %s" module-name)
        (if-let [s (intern module-ns 'stop)]
          (try+
           (s)
           (catch Exception ex
             (timbre/error ex))
           (finally
             (dosync
              (alter loader/modules dissoc module-name))))
          (throw+ "Module does not have a stop method")))
      (throw+ "Could not determine ns"))))
