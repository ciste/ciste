(ns ciste.runner
  "This is the runner for ciste applications.

  Specify this namespace as the main class of your application."
  (:require [ciste.config :refer [config describe-config]]
            [ciste.service :as service]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defonce application-promise (ref nil))

(describe-config [:ciste :logger]
  :string
  "A namespace containing a logging config")

(defn configure-logging
  []
  (try
    (let [logger (symbol (env :ciste-logger "ciste.logger"))]
     (require logger)
     ((ns-resolve logger 'set-logger)))
    (catch Exception ex
      (timbre/error "Could not set up logging" ex))))

(defn stop-application!
  []
  (timbre/info "Stopping application")
  (service/stop-services!)
  (if-let [p @application-promise]
    (deliver p true)
    (timbre/error "Application promise is nil")))

(defn start-application!
  ([] (start-application! nil))
  ([modules]
   (configure-logging)
   (timbre/infof "Starting application")
   (service/init-services modules)
   (dosync (ref-set application-promise (promise)))
   (timbre/info "application initialized")
   @application-promise))

(defn -main
  "Main entry point for ciste applications.

   Specify this function as you application's entry point."
  [& options]
  @(start-application!))
