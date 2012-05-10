(ns ciste.model
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>]])
  (:require [clj-http.client :as client]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.tools.logging :as log]
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.InputStream
           java.io.StringReader
           nu.xom.Builder
           nu.xom.Document
           nu.xom.Node
           org.xml.sax.InputSource))

;; TODO: find a better place
(defmacro implement
  "Throws an exception saying that this function has not been implemented"
  ([]
     `(throw (UnsupportedOperationException. "Not implemented yet")))
  ([& body]
     `(do
        (log/warn "Not implemented yet")
        ~@body)))

(defn string->zip
  "parse xml string as a zipper sequence"
  [^String xml]
  (-> xml StringReader. InputSource.
      xml/parse zip/xml-zip))

(defn ^Document stream->document
  "Read an input stream into a XOM document"
  [^InputStream input-stream]
  (.build (Builder.) input-stream))

(defn ^Document string->document
  "Read a string into a XOM document"
  [^String xml]
  (.build (Builder.) xml ""))

(defn ^String fetch-resource
  "Fetch the url, return the string"
  [^String url]
  (log/infof "fetching resource: %s" url)
  (if-let [response (try (client/get url)
                         #_(catch Exception ex
                           (log/error ex)))]
    (let [{:keys [body status]} response]
      (when (not (#{404 500} status))
        body))))

(defn ^Document fetch-document
  "Fetch the url and return it as a XOM document"
  [^String url]
  (-?> url fetch-resource string->document))

;; TODO: reverse order
(defn query
  "Return the sequence of nodes that match the xpath expression"
  [^String path ^Node doc]
  (let [nodes (.query doc path)]
    (map
     #(.get nodes %)
     (range (.size nodes)))))

(defn get-links
  [url]
  (-> url
      fetch-resource
      StringReader.
      enlive/html-resource
      (enlive/select [:link])))
