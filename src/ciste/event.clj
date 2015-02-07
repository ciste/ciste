(ns ciste.event
  (:require [clojurewerkz.eep.emitter :as e]))

(def emitter
  (e/create {:dispatcher-type :ring-buffer}))

(defn notify
  [key msg]
  ;; TODO: make sure that the channel is reqistered
  (e/notify emitter key (assoc msg :event key)))
