(ns
  ciste.runner
  "This is the runner for ciste applications.

Specify this namespace as the main class of your application."
  (:use [ciste.config :only [config load-config run-initializers!
                             set-environment!]]
        [ciste.debug :only [spy]]
        [lamina.core
         ;; :only [enqueue on-drained permanent-channel receive-in-order receive-all]
         ]
        lamina.executor)
  (:require [clojure.tools.logging :as log])
  (:import java.io.FileNotFoundException
           java.util.concurrent.ConcurrentLinkedQueue))

(defonce application-promise (ref nil))

(defonce
  ^{:doc "By default, the runner will look for a file with this name at the root
          of the project directory."}
  default-site-config-filename "ciste.clj")

(defonce
  ^{:doc "Ref containing the currently loaded site config"}
  default-site-config (ref {}))

(defonce pending-requires (ConcurrentLinkedQueue.))

(defn consume-require
  [sym]
  (try
    #_(log/debugf "Loading %s" sym)
    (require sym)
    #_(log/debugf " - %s loaded" sym)
    (catch Exception ex
      (log/error ex)
      (.printStackTrace ex)
      (System/exit 0))))


(defn read-site-config
  "Read the site config file"
  ([] (read-site-config default-site-config-filename))
  ([filename]
     (try
       ;; TODO: Check a variety of places for this file.
       (-> filename slurp read-string)
       (catch FileNotFoundException ex
         ;; TODO: Throw an exception here
         (throw (RuntimeException.
                 "Could not find service config. Ensure that ciste.clj exists at the root of your application and is readable"))))))

(defn load-site-config
  "Read the site config and store it for later use"
  []
  (let [site-config (read-site-config)]
    (dosync
     (ref-set default-site-config site-config))))

(defn require-namespaces
  "Require the sequence of namespace strings"
  [namespaces]
  (doseq [sn namespaces]
    (let [sym (symbol sn)]
      #_(log/debugf "enqueuing %s" sym)
      (.add pending-requires sym))))

(defn require-modules
  "Require each namespace"
  ([] (require-modules @default-site-config))
  ([service-config]
     (require-namespaces (concat (:modules service-config)
                                 (:services service-config)
                                 (config :modules)
                                 (config :services)))))

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

(defn process-requires
  []
  (loop [sym (.poll pending-requires)]
    (when sym
      (consume-require sym)
      (recur (.poll pending-requires)))))

(defn init-services
  "Ensure that all namespaces for services have been required and that the
   config provider has benn initialized"
  [environment]
  ;; TODO: initialize config backend
  (load-config)
  (set-environment! environment)
  (require-modules)
  (run-initializers!)
  (process-requires)
  )

(defn stop-services!
  ([] (stop-services! @default-site-config))
  ([site-config]
     (doseq [service-name (concat (:services site-config)
                                  (config :services))]
       (log/info (str "Stopping " service-name))
       ((intern (the-ns (symbol service-name)) (symbol "stop"))))))

(defn stop-application!
  []
  (log/info "Stopping application")
  (stop-services!)
  (deliver @application-promise true))

(defn start-application!
  ([]
     (start-application! (:environment @default-site-config)))
  ([environment]
     (log/info "Starting application")
     (init-services environment)
     (dosync (ref-set application-promise (promise)))
     ;; (run-initializers!)
     (start-services!)
     @application-promise))

(defn -main
  "Main entry point for ciste applications.

   Specify this function as you application's entry point."
  [& options]
  (load-site-config)
  (start-application!))
