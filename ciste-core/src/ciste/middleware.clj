(ns ciste.middleware
  (:use [ciste.config :only [config]])
  (:require [clojure.pprint :as p]
            [clojure.tools.logging :as log]))

(defn wrap-http-serialization
  [handler]
  (fn [request]
    (->> request
         (merge {:serialization :http, :format :html})
         handler)))

(defn with-request-logging
  "Log each request"
  [handler]
  (fn [request]
    (p/pprint request)
    (handler request)))

(defn apply-wrappers
  "wraps the action with middleware contained in wrappers"
  [action wrappers]
  (if wrappers
    (reduce #(%2 %1) (apply vector action wrappers))
    action))

(defn wrap-log-request
  [handler]
  (fn [request]
    (if (config :print :request)
      (log/spy :info (dissoc request :aleph.http/channel)))
    (handler request)))

(defn wrap-log-params
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (do
        (if (config :print :params)
          (p/pprint response))
        response))))

