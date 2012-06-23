(ns ciste.service.tigase
  (:use [ciste.config :only [config describe-config]]
        [clj-tigase.core :only [get-config start-router!]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]))

;; TODO: Pull this list from a UserRole collection
(describe-config [:admins]    :list
  "A list of usernames that are considered admins of the system.")

(describe-config [:xmpp :auth-db] :string
  "The name of a class implementing the auth repository")

(describe-config [:xmpp :c2s] :number
  "The client to server port for the xmpp service")

(describe-config [:xmpp :s2s] :number
  "The server to server port for the xmpp service")

(describe-config [:xmpp :user-db] :string
  "The name of a class implementing the user repository")

(def ^:dynamic *initial-config* "")

(defn tigase-options
  []
  (into-array
      String
      (concat ["--admins" (->> (config :admins)
                               (map (fn [username]
                                      ;; TODO: ensure user created
                                      (str username "@" (config :domain))))
                               (string/join "," ))
               "--auth-db" (config :xmpp :auth-db)
               "--user-db" (config :xmpp :user-db)
               "--debug" "server"
               "--sm-plugins" (string/join "," (config :xmpp :plugins))
               "--c2s-ports" (string/join "," (config :xmpp :c2s))
               "--s2s-ports" (string/join "," (config :xmpp :s2s))
               ;; TODO: should we support multiple virtual domains here?
               "--virt-hosts" (config :domain)]
              (flatten
               (map-indexed
                (fn [n component]
                  [(str "--comp-name-" (inc n))
                   (:name component)
                   (str "--comp-class-" (inc n))
                   (:class component)])
                (config :xmpp :components))))))

(defn start
  []
  (let [tigase-config (get-config *initial-config* (tigase-options))]
    (try
      (start-router! tigase-options tigase-config)
      (catch Exception ex
        (log/error (.getRootCause ex))
        (System/exit 0)))))
