(ns ciste.filters)

(defn filter-action-dispatch
  "Dispatch function for filter-action.

  Returns a vector containing the Action (the first param) and the
  :serialization key from the second param (The request object)."
  [action request]
  [action (:serialization request)])

(defmulti filter-action filter-action-dispatch)

(defmacro deffilter
  "Define a filter for the Action for the given serialization type.

   When a route is being resolved, the filter will be called instead of the
   action.

   It is the job of the filter to deserialize the request object and call the
   provided action function with the required arguments."
  [action serialization binding-form & body]
  `(defmethod ciste.filters/filter-action [~action ~serialization]
     ~binding-form
     ~@body))
