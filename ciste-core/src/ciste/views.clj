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
  (:require [clojure.tools.logging :as log]))

(defn view-dispatch
  [{:keys [action format]} & args]
  [action format])

(defmulti
  ^{:doc "Return a transformed response map for the action and format"}
  apply-view view-dispatch)

(defmulti apply-view-by-format
  (fn [{:keys [format]} & _] format))

(defmacro defview
  "Define a view for the action with the specified format"
  [action format args & body]
  `(defmethod ciste.views/apply-view [~action ~format]
     ~args
     ~@body))

(defmethod apply-view :default
  [request & args]
  (try
    (apply apply-view-by-format request args)
    (catch IllegalArgumentException e
      (throw (IllegalArgumentException.
              (str "No view defined to handle ["
                   (:action request) " "
                   (:format request) "]") e)))))
