(ns ciste.formats.default
  (:use (ciste [formats :only (format-as)]))
  (:require (clojure.data [json :as json]
                          [xml :as xml])))

(defmethod format-as :default
  [format request response]
  response)

(defmethod format-as :atom
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/atom+xml")))

(defmethod format-as :clj
  [format request response]
  (-> response
      (assoc-in  [:headers "Content-Type"] "text/plain")
      (assoc :body (str (:body response)))))

(defmethod format-as :html
  [format request response]
  response)

(defmethod format-as :json
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/json")
      (assoc :body (json/json-str (:body response)))))

(defmethod format-as :n3
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "text/n3;charset=utf-8")))

(defmethod format-as :rdf
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/rdf+xml")))

(defmethod format-as :xml
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xml")
      (assoc :body (with-out-str (xml/emit (:body response))))))

(defmethod format-as :xmpp
  [format request response]
  response)

