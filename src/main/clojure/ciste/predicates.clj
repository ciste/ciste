(ns ciste.predicates
  (:require compojure.core
            clout.core))

(defn http-serialization?
  [request matcher]
  (if (= (:serialization request) :http)
    request))

(defn method-matches?
  [request matcher]
  (and (#'compojure.core/method-matches
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
    (if-let [response (clout.core/route-matches
                       (clout.core/route-compile (:node matcher))
                       (:node request))]
      (assoc request :params response))
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
  (if (#'compojure.core/method-matches (:method matcher) request)
    request))

(defn type-matches?
  [request matcher]
  (if (:method matcher)
    (if (= (:method matcher) (:method request))
      request)
    request))

(defn xmpp-serialization?
  [request matcher]
  (if (= (:serialization request) :xmpp)
    request))

