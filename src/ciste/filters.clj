(ns ciste.filters)

(defn filter-action-dispatch
  [action request]
  [action (:serialization request)])

(defmulti filter-action filter-action-dispatch)

(defmacro deffilter
  [action serialization binding-form & body]
  `(defmethod ciste.filters/filter-action [~action ~serialization]
     ~binding-form
     ~@body))
