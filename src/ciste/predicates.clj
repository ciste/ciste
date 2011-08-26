(ns ciste.predicates
  (:use ciste.debug)
  (:require compojure.core
            clout.core))

;; From old versions of compojure
;; TODO: find out where this went
(defn- method-matches*
  "True if this request matches the supplied method."
  [method request]
  (let [request-method (request :request-method)
        form-method (get-in request [:form-params "_method"])]
    (or (nil? method)
        (if (and form-method (= request-method :post))
          (= (.toUpperCase (name method)) form-method)
          (= method request-method)))))


(defn method-matches?
  [request matcher]
  (and (method-matches*
        (:method matcher) request)
       request))

(defn name-matches?
  [request matcher]
  (if (:name matcher)
    (if (= (:name matcher) (:name request))
      request)
    request))

(defn node-matches?
  [request matcher]
  (if (:node matcher)
    (if (:node request)
      (if-let [response (clout.core/route-matches
                         (:node matcher)
                         {:uri (:node request)})]
        (assoc request :params response)))
    request))

(defn ns-matches?
  [request matcher]
  (if (:ns matcher)
    (if (= (:ns matcher) (:ns request))
      request)
    request))

(defn path-matches?
  [request matcher]
  (if-let [path (:path matcher)]
    (if-let [route-params (clout.core/route-matches path request)]
      (#'compojure.core/assoc-route-params request route-params))))

(defn request-method-matches?
  [request matcher]
  (if (method-matches* (:method matcher) request)
    request))

(defn type-matches?
  [request matcher]
  (if (:method matcher)
    (if (= (:method matcher) (:method request))
      request)
    request))

(defn http-serialization?
  [request matcher]
  (if (= (:serialization request) (:serialization matcher) :http)
    request))

(defn xmpp-serialization?
  [request matcher]
  (if (= (:serialization request) (:serialization matcher) :xmpp)
    request))

