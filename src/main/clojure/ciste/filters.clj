(ns ciste.filters)

(defmulti apply-filter (fn [action request] [action (:serialization request)]))

(defmacro deffilter
  [action serialization binding-form & body]
  `(defmethod ciste.filters/apply-filter [~action ~serialization]
     ~binding-form
     ~@body))
