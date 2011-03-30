(ns ciste.html
  (:use ciste.config
        ciste.core)
  (:require [clojure.pprint :as p]
            [hiccup.core :as h]))

(defn dump*
  [val]
  [:p
   [:code
    [:pre
     (h/escape-html
      (with-out-str
        (p/pprint val)))]]])

(defn dump
  [val]
  (if (-> (config) :debug)
    (dump* val)))

(defn dump-unescaped
  [val]
  (if (-> (config) :debug)
    [:p
     [:pre
      [:code.prettyprint
       (h/escape-html
        val)]]]))

(defn link-to-script
  [href]
  [:script
   {:type "text/javascript"
    :lang "javascript"
    :src href}])

(defn link-to-stylesheet
  [href]
  [:link
   {:type "text/css"
    :href href
    :rel "stylesheet"
    :media "screen"}])

(defmethod format-as :html
  [format request response]
  response)

(defmethod serialize-as :http
  [serialization text]
  text)

