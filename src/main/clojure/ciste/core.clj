(ns ciste.core
  (:require [ciste.triggers :as triggers]))

(defonce #^:dynamic *format* nil)
(defonce #^:dynamic *serialization* nil)

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
     (let [~args params#
           records# (do ~@forms)]
       (triggers/run-triggers (var ~name) params# records#)
       records#)))

(defmulti serialize-as (fn [x & _] x))

(defmulti apply-template (fn [request response] (:format request)))

(defmethod apply-template :default
  [request response]
  response)
