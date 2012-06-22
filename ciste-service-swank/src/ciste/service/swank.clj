(ns ciste.service.swank
  (:use [ciste.config :only [config describe-config]])
  (:require [swank.swank :as swank]))

(describe-config [:swank :port]
  :string
  "The port the swank connector should listen on.

The swank connector expects a string, so that is what we accept for now.")


(defn start
  "Start a swank connector"
  []
  (swank/start-repl (or (config :swank :port) "4005")))
