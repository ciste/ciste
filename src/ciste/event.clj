(ns ciste.event
  (:require [clojurewerkz.eep.emitter :as e]
            [slingshot.slingshot :refer [throw+]]))

(def ^:dynamic *keys* (ref {}))

(def emitter
  (e/create {:dispatcher-type :ring-buffer}))

(defn notify
  [key msg]
  ;; TODO: make sure that the channel is reqistered
  (if-let [d (get @*keys* key)]
    (e/notify emitter key (assoc msg :event key))
    (throw+ (str "No description for key: " key))))

(defn defkey
  [key desc & {:as body}]
  (dosync
   (alter *keys* assoc key (assoc body :desc desc))))
