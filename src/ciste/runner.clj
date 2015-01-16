(ns ciste.runner
  "This is the runner for ciste applications.

Specify this namespace as the main class of your application."
  (:use [ciste.config :only [config default-site-config load-config load-site-config set-environment!]]
        [ciste.initializer :only [run-initializers!]]
        [lamina.core :only [enqueue on-drained permanent-channel
                            receive-in-order receive-all]]
        lamina.executor)
  (:require [ciste.service :as service]
            [clojure.tools.logging :as log])
  (:gen-class))

(defonce application-promise (ref nil))

(defn stop-application!
  []
  (log/info "Stopping application")
  (service/stop-services!)
  (deliver @application-promise true))

(defn start-application!
  ([]
     (start-application! (or (System/getenv "CISTE_ENV")
                             (:environment @default-site-config))))
  ([environment]
     (log/info "Starting application")
     (service/init-services environment)
     (dosync (ref-set application-promise (promise)))
     ;; (run-initializers!)
     ;; (service/start-services!)
     @application-promise))

(defn -main
  "Main entry point for ciste applications.

   Specify this function as you application's entry point."
  [& options]
  (load-site-config)
  (start-application!))
