(ns
    ^{:author "Daniel E. Renfer <duck@kronkltd.net>"
      :doc "## Routing

Ciste's routing mechanism can be used in any situation where you have
a request that needs to be processed, possibly changing state,
returning a result that is then transformed into a desired output
format, and then either returned or processed in some other fashion.

'resolve-routes' takes 2 parameters: a sequence of predicates, and a
sequence of matcher pairs. A \"handler\" function is then returned that
takes a request map and then returns a response.

When a request is being processed, Ciste will iterate over the
sequence of matchers and apply the predicates. The first
matcher to return a non-nil result will then invoke its action.

A matcher pair is a sequence containing 2 maps. The first map contains
data that will be used by the predicates to determine if the request
is valid for the matcher. The section map contains information that
will be used if the matcher is selected.

The predicate sequence is a list of predicate functions. Each function
takes the matcher data as the first argument and the request as the
second. Each predicate will perform some test, possibly using data
contained in the matcher map as its arguments. If the predicate
passes, it returns a map containing the new request map for the next
step in the chain. Usually the request is simply returned unmodified.

## Invoking an Action

When a Ciste route is matched, invoke-action will perform a series of
steps, ultimately returning the final result.

First, the Filter is called. The Filter will extract all of the
necessary parameters from the serialization-specific request and call
the serialization-agnostic Action. The Action will produce a result,
which is then returned by the Filter.

Next, the request map and the returned data are passed to the View
function. Views are specific to the Format in use. The View will
transform the response data to a format acceptable to the downstream
Serializer.

With the response data transformed into a format-specific view, a
template is then called, if enabled. This will attach any additional
markup or perform any processing that is done to every request using
the same format that specifies that a template be used.

The next stage is to call the Formatter. This is the last stage that
is specific to the format. This is where any intermediate data
structures are converted to types that can be used by
serializers. Steps such as converting Hiccup vectors to strings should
be done here.

Finally, the Serializer performs a last stage transform specific to
the Serialization type. Place things that need to apply to every
request here. If Ciste is being used in a Ring application, there is
no need to perform any IO, and the map can simply be returned. It is
possible to write Serializers that will respond to a request by
transmitting the response in any number of ways. (XMPP, Email,
Filesystem, etc.) 

"
      }
    ciste.routes
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
