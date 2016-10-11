(ns ciste.event
  (:require [manifold.bus :as bus]
            [slingshot.slingshot :refer [throw+]]))

(defonce ^:dynamic *keys* (ref {}))

(defonce events (bus/event-bus))

(defn notify
  [topic msg]
  (bus/publish! events topic (assoc msg :event topic)))

(defn defkey
  [topic desc & {:as body}]
  (dosync
   (alter *keys* assoc topic (assoc body :desc desc))))
