(ns ciste.factory)

(def #^:dynamic *counters* (ref {}))

(defn set-counter!
  [type value]
  (get
   (dosync
    (alter
     *counters*
     (fn [m] (assoc m type value))))
   type))

(defn reset-counter!
  [type]
  (set-counter! type 0))

(defn get-counter
  [type]
  (get @*counters* type 0))

(defn inc-counter!
  [type]
  (dosync
   (alter
    *counters*
    (fn [m]
      (assoc m type (inc (get m type 0)))))))

(defn next-counter!
  [type]
  (get (inc-counter! type) type))

(defmulti fseq (fn [type & _] type))

(defmulti factory (fn [type & _] type))

(defmacro deffactory
  [type opts & body]
  `(defmethod ciste.factory/factory ~type
     [_# & opts#]
     (merge (new ~type)
            (into {}
                  (map
                   (fn [[k# v#]]
                     [k# (if (ifn? v#)
                           (v#) v#)])
                   ~opts))
            (first opts#))))

(defmacro defseq
  [type let-form result]
  `(let [type# ~type]
     (defmethod ciste.factory/fseq type#
       [type#]
       (let [~let-form [(next-counter! type#)]]
         ~result))))
