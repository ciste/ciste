(ns ciste.runner
  "This is the runner for ciste applications.

  Specify this namespace as the main class of your application."
  (:require [ciste.config :refer [load-site-config]]
            [ciste.service :as service]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]])
  (:gen-class))

(defonce application-promise (ref nil))

(defn stop-application!
  []
  (log/info "Stopping application")
  (service/stop-services!)
  (deliver @application-promise true))

(defn start-application!
  ([]
   (start-application! (env :ciste-env "default")))
  ([environment]
   (log/info (str "Starting application with environment: " environment))
   (service/init-services environment)
   ;; (service/start-services!)
   (dosync (ref-set application-promise (promise)))
   (log/info "application initialized")
   @application-promise))

(defn -main
  "Main entry point for ciste applications.

   Specify this function as you application's entry point."
  [& options]
  (load-site-config)
  @(start-application!))
