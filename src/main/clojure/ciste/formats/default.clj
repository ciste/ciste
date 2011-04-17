(ns ciste.formats.default
  (:use ciste.core)
  (:require [clojure.contrib.lazy-xml :as xml]))

(defmethod format-as :default
  [format request response]
  response)

(defmethod format-as :json
  [format request response]
  ;; TODO: "add json content type"
  (assoc
      (assoc-in response
                [:headers "Content-Type"]
                "application/json")
    :body (json/json-str (:body response))))

(defmethod format-as :rdf
  [format request response]
  ;; TODO: "add json content type"
  (assoc-in response [:headers "Content-Type"] "application/rdf+xml"))

(defmethod format-as :n3
  [format request response]
  ;; TODO: "add json content type"
  (assoc-in response [:headers "Content-Type"] "text/n3"))

(defmethod format-as :xml
  [format request response]
  {:headers {"Content-Type" "application/xml"}
   :body (with-out-str (xml/emit (:body response)))})

(defmethod format-as :clj
  [format request response]
  (assoc-in response :headers "Content-Type" "text/plain"))

(defmethod format-as :atom
  [format request response]
  ;; TODO: "add atom content type"
  (merge {:headers {"Content-Type" "application/atom+xml"}}
         response)
  #_(let [document
          (if (= (:type response) :feed)
            (atom-feed response)
            (atom-entry response))]
      {:body (with-out-str (.writeTo document *out*))}))
