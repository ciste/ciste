(ns ciste.routes
  (:use (ciste config core debug filters)
        (clojure.core [incubator :only (-?>>)]))
  (:require (ciste [formats :as formats]
                   [views :as views])
            (clojure.tools [logging :as log])))

(defn lazier
  "This ensures that the lazy-seq will not be chunked

Contributed via dnolan on IRC."
  [coll]
  (when-let [s (seq coll)]
    (lazy-seq (cons (first s) (lazier (next s))))))

(defn make-matchers
  [handlers]
  (map
   (fn [[matcher action]]
     (let [[method route] matcher]
       [{:method method
         :format :http
         :serialization :http
         :path route}
        {:action action
         :serialization :http
         :format :html}]))
   handlers))

(defn try-predicate
  "Tests if the request and matcher info matches the predicate.

If the predicate is a sequence, test the first first element. If that test
succeeds, continue with the remainder of the sequence.

If the predicate is a function, apply that function against the request and
matcher.

If the request is still non-nil after walking the entire predicate sequence,
then the route is considered to have passed."
  [request matcher predicate]
  (if predicate
    (if (coll? predicate)
      (if (empty? predicate)
        request
        (if-let [request (try-predicate request matcher (first predicate))]
          (recur request matcher (rest predicate))))
      (if (ifn? predicate)
        (let [response (predicate request matcher)]
          (if (config :print :predicates)
            (if-let [matched (not (nil? response))]
              (log/debug (str predicate " => " matched))))
          response)))))

(defn try-predicates
  "Tests if the request and the matcher info matches the provided predicates.

Returns either a (possibly modified) request map if successful, or nil."
  [request matcher predicates]
  (if (config :print :matchers)
    (spy matcher))
  (->> predicates
       lazier
       (map #(try-predicate request matcher %))
       (filter identity)
       first))

(defn invoke-action
  "Renders the given action against the request"
  [request]
  (let [{:keys [format serialization action]} request]

    ;; Print as part of the pipeline
    (when (config :print :routes)
      (log/info (str action " " format " " (:params request))))
    (with-context [serialization format]
      (-?>> request
           (filter-action action)
           (views/apply-view request)
           (apply-template request)
           (formats/format-as format request)
           (serialize-as (:serialization request))))))

(defn resolve-route
  "If the route matches the predicates, invoke the action"
  [predicates [matcher {:keys [action format serialization]}] request]
  (if-let [request (try-predicates request matcher (lazier predicates))]
    (let [format (or (keyword (:format (:params request)))
                     format (:format request))
          serialization (or serialization (:serialization request))
          request (merge request
                         {:format format
                          :action action
                          :serialization serialization})]
      (invoke-action request))))

(defn resolve-routes
  "Returns a handler fn that will match each route against
the predicate sequence and return the result of the invoking the
first match."
  [predicates routes]
  (fn [request]
    (->> routes
         lazier
         (map #(resolve-route predicates % request))
         (filter identity)
         first)))
