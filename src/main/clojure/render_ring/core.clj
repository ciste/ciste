(ns render-ring.core
  (:use clojure.contrib.pprint
        clojure.contrib.lazy-xml)
  (:require [clojure.contrib.json :as json]
            [clojure.contrib.str-utils :as str-util]
            [hiccup.page-helpers :as p]))

(defmacro defpage
  [action format args & body]
  `(defmethod render-ring.core/render [~action ~format]
     [_# _# ~@args]
     ~@body))

(defmulti render
  (fn [action format & args] [action format]))

(defmulti default-render
  (fn [_ format & _] format))

(defmethod render :default
  [action format & args]
  (apply default-render action format args))

(defmethod default-render :json
  [action format & args]
  {:headers {"Content-Type" "application/json"}
   :body (apply json/json-str args)})

(defmethod default-render :xml
  ;; {:doc "Attempts to render an unsupported type as xml"}
  [action format & args]
  {:body
   {:tag
    (keyword
     (last
      (str-util/re-split
       #"\."
       (str (:ns (meta action))))))
    :attrs nil
    :content nil}})

(defmethod default-render :clj
  [action format & args]
  {:body
   (with-out-str (pprint (first args)))})

(defmulti template (fn [format handler request] format))

(defmethod template :html
  [format handler request]
  {:body
   (list
    (p/doctype :xhtml-strict)
    [:html {:xmlns "http://www.w3.org/1999/xhtml"}
     (handler request)])})

(defmethod template :json
  [format request response]
  ;; TODO: "add json content type"
  response)

(defmethod template :xml
  [format request response]
  ;; TODO: "add xml content type"
  (do #_(content-type "application/xml")
      {:body (with-out-str (emit (:body response)))}))

(defmethod template :clj
  [format request response]
  ;; TODO: "add plain content type"
  (do #_(content-type "text/plain")
      response))

(defmethod template :atom
  [format request response]
  ;; TODO: "add atom content type"
  (do #_(content-type "application/atom+xml")
      #_(let [document
              (if (= (:type response) :feed)
                (atom-feed response)
                (atom-entry response))]
          {:body (with-out-str (.writeTo document *out*))})))

(defn with-request-logging
  "Log each request"
  [handler]
  (fn [request]
    (pprint request)
    (handler request)))

(defn apply-wrappers
  "wraps the action with middleware contained in wrappers"
  [action wrappers]
  (if wrappers
    (reduce
     #(%2 %1)
     (apply vector action wrappers))
    action))

;; TODO: "If the action returns nil, should the render action still be invoked?"
(defn with-render
  "Middleware function.

This function expects a handler function that returns a map containing an action
var, a format identifier, and a seq of arguments.

The action var is invoked with the arguments and the result is passed to the
render function."
  [handler]
  (fn [request]
    (if-let [{:keys [action format args wrappers]} (handler request)]
      (do
        (println action " " format)
        (comment (println "Action: " action)
                 (println "Format: " format)
                 (println "Request: " request)
                 (println "Wrappers: " wrappers))
        (let [records ((apply-wrappers action wrappers) request)]
          (render action format records))))))
