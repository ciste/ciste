(ns
    ^{:doc "This is the runner for ciste applications.

Specify this namespace as the main class of your application."}
  ciste.runner
  (:use [ciste.config :only [config load-config set-environment!]]
        [ciste.debug :only [spy]])
  (:require [clojure.tools.logging :as log])
  (:import java.io.FileNotFoundException))

(defonce
  ^{:doc "By default, the runner will look for a file with this name at the root
          of the project directory."}
  default-site-config-filename "ciste.clj")

(defonce
  ^{:doc "Ref containing the currently loaded site config"}
  default-site-config (ref {}))

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
  (future (doseq [sn namespaces]
            (log/info (str "Loading " sn))
            (try
              (require (symbol sn))
              (catch Exception ex
                (log/error ex)
                (System/exit 0))))))

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
  [site-config]
  (doseq [service-name (concat (:services site-config)
                               (config :services))]
    (let [service-sym (symbol service-name)]
      (log/info (str "Starting " service-name))
      (require service-sym)
      ((intern (the-ns service-sym) (symbol "start"))))))

(defn init-services
  "Ensure that all namespaces for services have been required and that the
   config provider has benn initialized"
  [site-config environment]
  ;; TODO: initialize config backend
  (load-config)
  (set-environment! environment)
  (require-modules site-config))

(defn stop-services!
  ([] (stop-services! @default-site-config))
  ([site-config]
     (doseq [service-name (:services site-config)]
       (log/info (str "Stopping " service-name))
       ((intern (the-ns (symbol service-name)) (symbol "stop"))))))

(defn stop-application!
  []
  (stop-services!))

(defn -main
  "Main entry point for ciste applications.

   Specify this function as you application's entry point."
  [& options]
  (let [opts (apply hash-map options)
        site-config (load-site-config)
        
        ;; TODO: allow this to be passed in via command line
        environment (:environment site-config)]

    (init-services site-config environment)
    ;; (Thread/sleep 6000)
    (start-services! site-config)
    
    ;; TODO: store this and allow it for shutdown.
    @(promise)))
