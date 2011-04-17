(ns ciste.views)

(defn view-dispatch
  [{:keys [action format]} & args]
  [action format])

(defmulti apply-view view-dispatch)

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
      (raise (IllegalArgumentException.
              (str "No view defined to handle ["
                   (:action request) " "
                   (:format request) "]"
                   ) e)))))

(defmulti apply-view-by-format
  (fn [{:keys [format]} & _] format))
