(ns ciste.core
  (:use ciste.config
        lamina.core)
  (:require [ciste.triggers :as triggers]))

(defonce ^:dynamic *format* nil)
(defonce ^:dynamic *serialization* nil)
(defonce ^:dynamic *actions* (permanent-channel))
(receive-all *actions* (fn [_]))

(defmacro with-serialization
  [serialization & body]
  `(binding [*serialization* ~serialization]
     ~@body))

(defmacro with-format
  [format & body]
  `(binding [*format* ~format]
     ~@body))

(defmacro defaction
  [name args & forms]
  `(defn ~name
     [& params#]
     (if (-> (#'config) :print :action)
       (clojure.tools.logging/info (str (var ~name))))
     (let [~args params#
           action# (var ~name)
           records# (do ~@forms)]
       (enqueue *actions* {:action action#
                           :args params#
                           :records records#})
       (triggers/run-triggers action# params# records#)
       records#)))

(defmulti serialize-as (fn [x & _] x))

(defmulti apply-template (fn [request response] (:format request)))

(defmethod apply-template :default
  [request response]
  response)
