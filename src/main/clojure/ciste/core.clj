(ns ciste.core
  (:use ciste.config
        ciste.debug
        ciste.filters
        ciste.view
        clojure.pprint
        [clojure.contrib.logging :only (info)])
  (:require [ciste.trigger :as trigger]))

(defonce #^:dynamic *matchers* (ref []))
(defonce #^:dynamic *routes* (ref []))

(defmacro defaction
  [name args & forms]
  `(defn ~name ~args ~@forms))

(defmacro defview
  [action format args & body]
  `(defmethod ciste.core/apply-view [~action ~format]
     ~args
     ~@body))

(defn lazier
  "This ensures that the lazy-seq will not be chunked

Contributed via dnolan on IRC."
  [coll]
  (when-let [s (seq coll)]
    (lazy-seq (cons (first s) (lazier (next s))))))

(defmulti default-format
  (fn [{:keys [format]} & _] format))

(defmulti apply-view
  (fn [{:keys [action format]} & args] [action format]))

(defmethod apply-view :default
  [request & args]
  (apply default-format request args))

(defmulti format-as (fn [format handler request] format))

(defmulti serialize-as (fn [x & _] x))

(defmulti apply-template (fn [request response] (:format request)))

(defmethod apply-template :default
  [request response]
  response)

;; middleware

(defn make-matchers
  [handlers]
  (map
   (fn [[matcher action]]
     (let [[method route] matcher]
       [{:method method
         :format :http
         :serialization :http
         :path route} action]))
   (partition 2 handlers)))

(defn try-matcher
  [request predicate matcher]
  (if predicate
    (if (coll? predicate)
      (if (empty? predicate)
        request
        (if-let [request (try-matcher request (first predicate) matcher)]
          (recur request (rest predicate) matcher)))
      (if (ifn? predicate)
        (let [response (predicate request matcher)]
          (if (-> (config) :print :predicates)
            (println predicate " => " (not (nil? response))))
          response)))))

(defn try-matchers
  [request predicates matcher]
  (if (-> (config) :print :matchers)
    (spy matcher))
  (first
   (filter
    identity
    (map
     (fn [predicate]
       (try-matcher request predicate matcher))
     (lazier predicates)))))

(defn resolve-route
  [[matcher action] request]
  (if-let [request (try-matchers request (lazier @*matchers*) matcher)]
    (let [request (assoc request :action action)
          format (or (keyword (:format (:params request)))
                     (:format request))]
      (with-format format
        (with-serialization (:serialization request)
          (let [request (assoc request :format format)]
            (info (str action " " format " " (:params request)))
            (if-let [records (apply-filter action request)]
              (do
                (trigger/run-triggers action request records)
                (if-let [response (apply-view request records)]
                  (let [templated (apply-template request response)
                        formatted (format-as format request templated)]
                    (serialize-as (:serialization request) formatted)))))))))))

(defn resolve-routes
  [routes]
  (fn [request]
    (println "")
    (first
     (filter
      identity
      (map
       #(resolve-route % request)
       (lazier routes))))))
