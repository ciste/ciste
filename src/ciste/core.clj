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

(defmacro with-context
  [[serialization format] & body]
  `(with-serialization ~serialization
    (with-format ~format
      ~@body)))

(defmacro defaction
  [name args & forms]
  `(defn ~name
     [& params#]
     (if (-> (#'config) :print :action)
       (clojure.tools.logging/info (str (var ~name))))
     (let [~args params#
           action# (var ~name)
           records# (do ~@forms)]
       ;; TODO: Find a good way to hook these kind of things
       (if (config :use-pipeline)
         (enqueue *actions* {:action action#
                             :args params#
                             :records records#}))
       (if (config :run-triggers)
         (triggers/run-triggers action# params# records#))
       records#)))

(defmulti serialize-as (fn [x & _] x))

(defmulti apply-template (fn [request response] (:format request)))

(defmethod apply-template :default
  [request response]
  response)
