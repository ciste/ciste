(ns ciste.runner
  "This is the runner for ciste applications.

  Specify this namespace as the main class of your application."
  (:require [ciste.config :refer [config describe-config set-environment!]]
            [ciste.service :as service]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defonce application-promise (ref nil))

(describe-config [:ciste :logger]
  :string
  "A namespace containing a logging config")

(defn configure-logging
  [environment]
  (let [logger (symbol (env :ciste-logger "ciste.logger"))]
    (require logger)
    ((ns-resolve logger 'set-logger))))

(defn stop-application!
  []
  (timbre/info "Stopping application")
  (service/stop-services!)
  (if-let [p @application-promise]
    (deliver p true)
    (timbre/error "Application promise is nil")))

(defn start-application!
  ([] (start-application! (env :ciste-env "default")))
  ([environment] (start-application! environment nil))
  ([environment modules]
   (set-environment! environment)
   (configure-logging environment)
   (timbre/with-context {:env environment}
     (timbre/infof "Starting application with environment: %s" environment))
   (service/init-services environment modules)
   ;; (service/start-services!)
   (dosync (ref-set application-promise (promise)))
   (timbre/info "application initialized")
   @application-promise))

(defn -main
  "Main entry point for ciste applications.

   Specify this function as you application's entry point."
  [& options]
  @(start-application!))
