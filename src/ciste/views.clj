(ns ciste.views
  "A View is a pair of multi-methods: apply-view, and default-format. The
apply-view method dispatches on a vector containing the Action and the
Format. If no match is found this value, then default-format tries
using only Format.

A View accepts two parameters: the request, and the response from
invoking the action. A View should render the supplied data into a
structure appropriate to the Format. It is not required, but this is
most commonly a map.

Example:

    (defview #'show :html
      [request user]
      {:status 200
       :body [:div.user
               [:p (:name user)]]})"
  (:use [ciste.core :only [*format*]])
  (:require [clojure.tools.logging :as log]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+]]))

(defn view-dispatch
  [{:keys [action]} & _]
  [action *format*])

(defmulti
  ^{:doc "Return a transformed response map for the action and format"}
  apply-view view-dispatch)

(defmulti
  ^{:doc "Fallback view for when no view can be found for the action"}
  apply-view-by-format (fn [& _] *format*))

(defmacro defview
  "Define a view for the action with the specified format"
  [action format binding-form & body]
  `(defmethod ciste.views/apply-view [~action ~format]
     [& args#]
     (let [~binding-form args#]
       (trace/trace :ciste:views:run
                    {:event :ciste:views:run
                     :request (first args#)
                     :response (rest args#)})
       ~@body)))

(defmethod apply-view :default
  [request & args]
  (apply apply-view-by-format request args))

(defmethod apply-view-by-format :default
  [request & _]
  (throw+
   {:message "No view defined"
    :action (:action request)
    :format *format*}))
