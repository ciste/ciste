(ns ciste.service
  (:require [ciste.config :refer [config config* load-config!]]
            [ciste.loader :as loader]
            [clojure.string :as string]
            [environ.core :refer [env]]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

(defn start-services!
  "Start each service."
  ([] (start-services! nil))
  ([modules]
   (timbre/info "start-services!")
   (let [service-names (string/split (config* :modules) #",")]
     (doseq [service-name service-names]
       (let [service-sym (symbol service-name)]
         (timbre/with-context {:name service-name}
           (timbre/infof "Requiring Service - %s" service-name))
         (require service-sym)
         ((intern (the-ns service-sym) (symbol "start")))
         (timbre/info "Finished starting" service-name)))
     (timbre/infof "Services started - %s" (keys @loader/modules)))))

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
