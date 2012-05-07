(ns ciste.runner
  "This is the runner for ciste applications.

Specify this namespace as the main class of your application."
  (:use [ciste.config :only [load-config set-environment!]])
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

(defn require-modules
  "Require each namespace"
  ([] (require-modules @default-site-config))
  ([service-config]
     (doseq [sn (concat (:modules service-config)
                        (:services service-config))]
       (log/debug (str "Loading " sn))
       (require (symbol sn)))))

(defn start-services!
  "Start each service."
  [site-config]
  (doseq [service-name (:services site-config)]
    (log/info (str "Starting " service-name))
    ((intern (the-ns (symbol service-name)) (symbol "start")))))

(defn init-services
  "Ensure that all namespaces for services have been required and that the
   config provider has benn initialized"
  [site-config environment]
  ;; TODO: initialize config backend
  (load-config)
  (require-modules site-config)
  (set-environment! environment))

(defn stop-services!
  []
  
  )

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
    (start-services! site-config)
    
    ;; TODO: store this and allow it for shutdown.
    @(promise)))
