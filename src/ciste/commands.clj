(ns ciste.commands
  (:use [ciste.filters :only [deffilter]]
        [ciste.routes :only [resolve-routes]]
        [ciste.views :only [defview]])
  (:require [ciste.predicates :as pred]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

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


;; TODO: This should take only a single command map
(defn parse-command
  "Takes a sequence of key/value pairs and runs a command"
  [{:as command}]
  (let [{:keys [name args]} command]
    (log/infof "parsing command: %s %s" name
               (string/join " " args))
    ((->> @*commands*
          (map (fn [[k v]] [{:name k} {:action v
                                      :format :clj}]))
          (resolve-routes @*command-predicates*))
     (merge {:serialization :command}
            command))))

;; Should this return a set?
(defn command-names
  "The names of all the registered commands."
  []
  (sort (keys @*commands*)))

(add-command! "list-commands" #'command-names)

(deffilter #'command-names :command
  [action request]
  (action))

(defview #'command-names :text
  [request names]
  (string/join "\n" names))

(defview #'command-names :json
  [request names]
  {:body names})

