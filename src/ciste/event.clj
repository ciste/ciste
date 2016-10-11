(ns ciste.event
  (:require [manifold.bus :as bus]
            [slingshot.slingshot :refer [throw+]]))

(defonce ^:dynamic *keys* (ref {}))

(defonce events (bus/event-bus))

(defn notify
  [topic msg]
  ;; TODO: make sure that the channel is reqistered
  (if-let [d (get @*keys* topic)]
    (bus/publish! events topic (assoc msg :event topic))
    (throw+ {:msg (str "No description for key: " topic)})))

(defn defkey
  [topic desc & {:as body}]
  (dosync
   (alter *keys* assoc topic (assoc body :desc desc))))
