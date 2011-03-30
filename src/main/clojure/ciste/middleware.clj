(ns ciste.middleware
  (:require [clojure.pprint :as p]))

(defn wrap-vectored-params
  [handler]
  (fn [request]
    (handler
     (merge
      request
      {:query-params
       (into {}
             (map
              (fn [[k v]]
                [k (if (and (.endsWith k "[]")
                            (not (vector? v)))
                     [v] v)])
              (:query-params request)))}))))

(defn wrap-http-serialization
  [handler]
  (fn [request]
    (let [merged-request
          (merge {:serialization :http
                  :format :html} request)]
      (handler merged-request))))

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
    (reduce
     #(%2 %1)
     (apply vector action wrappers))
    action))

