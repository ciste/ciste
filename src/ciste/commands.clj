(ns ciste.commands
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]]
               [routes :only [resolve-routes]]
               [views :only [defview]]))
  (:require (ciste [predicates :as pred])
            (clojure [string :as string])))

(defonce
  ^{:dynamic true
    :doc "The sequence of commands that have been registered."}
  *commands*
  (ref {}))

(defonce
  ^{:dynamic true
    :doc "The sequence of predicates used for command dispatch.
          By default, commands are dispatched by name."}
  *command-predicates*
  (ref [#'pred/name-matches?]))

(defn add-command!
  "Adds the fn identified by var v as the command handler for the given name."
  [name v]
  (dosync
   (alter *commands* assoc name v)))

(defn parse-command
  "Takes a sequence of key/value pairs and runs a command"
  [& opts]
  (let [command (apply hash-map opts)
        {:keys [name args]} (spy command)]
    ((->> @*commands*
          spy
          (map (fn [[k v]] [{:name k} {:action v}]))
          (resolve-routes @*command-predicates*))
     (merge command
            {:format        :text
             :serialization :command}))))




(defn command-names
  "The names of all the registered commands."
  []
  (sort (keys @*commands*)))

(add-command! "commands-list" #'command-names)

(deffilter #'command-names :command
  [action request]
  (action))

(defview #'command-names :text
  [request names]
  (string/join "\n" names))

