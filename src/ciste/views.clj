(ns ciste.views)

(defn view-dispatch
  [{:keys [action format]} & args]
  [action format])

(defmulti apply-view view-dispatch)

(defmulti apply-view-by-format
  (fn [{:keys [format]} & _] format))

(defmacro defview
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
