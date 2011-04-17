(ns ciste.views.default
  (:use ciste.views)
  (:require [clojure.contrib.json :as json]
            [clojure.pprint :as p]
            [clojure.string :as string]))

(defmethod apply-view-by-format :json
  [{:keys [action format]} & args]
  {:body (apply json/json-str args)})

(defmethod apply-view-by-format :xml
  ;; {:doc "Attempts to render an unsupported type as xml"}
  [{:keys [action format]} & args]
  {:body
   {:tag
    (keyword
     (last
      (string/split
       (str (:ns (meta action)))
       #"\.")))
    :attrs nil
    :content nil}})

(defmethod apply-view-by-format :clj
  [{:keys [action format]} & args]
  {:body (with-out-str (p/pprint (first args)))})
