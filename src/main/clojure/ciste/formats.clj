(ns ciste.formats
  (:use ciste.core)
  (:require [clojure.contrib.json :as json]
            [clojure.contrib.lazy-xml :as xml]
            [clojure.pprint :as p]
            [clojure.string :as string]))

(defmethod default-format :json
  [{:keys [action format]} & args]
  (println "default-format called")
  {:headers {"Content-Type" "application/json"}
   :body (apply json/json-str args)})

(defmethod default-format :xml
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

(defmethod default-format :clj
  [{:keys [action format]} & args]
  {:body
   (with-out-str (p/pprint (first args)))})

(defmethod format-as :default
  [format request response]
  (println "default response: " response)
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
  ;; TODO: "add plain content type"
  (do #_(content-type "text/plain")
      response))

(defmethod format-as :atom
  [format request response]
  ;; TODO: "add atom content type"
  (merge {:headers {"Content-Type" "application/atom+xml"}}
         response)
  #_(do #_(content-type "application/atom+xml")
      #_(let [document
              (if (= (:type response) :feed)
                (atom-feed response)
                (atom-entry response))]
          {:body (with-out-str (.writeTo document *out*))})))

