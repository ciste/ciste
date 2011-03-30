(ns ciste.view)

(defonce #^:dynamic *format* nil)
(defonce #^:dynamic *serialization* nil)

(defn record-class
  "Returns the class of the first parameter"
  [record & others]
  [(class record)])

(defn record-class-seq
  "Returns the class of the first element of the first parameter"
  [records & others]
  [(class (first records))])

(defn record-class-serialization
  "Returns the class of the first parameter"
  [record format serialization & others]
  [(class record) format serialization])

(defn record-class-seq-serialization
  "Returns the class of the first element of the first parameter"
  [records format serialization & others]
  [(class (first records)) format serialization])

(defn record-class-format
  "Returns the class of the first parameter"
  [record format & others]
  [(class record) format])

(defn record-class-seq-format
  "Returns the class of the first element of the first parameter"
  [records format & others]
  [(class (first records)) format])

(defmacro with-serialization
  [serialization & body]
  `(binding [*serialization* ~serialization]
     ~@body))

(defmacro with-format
  [format & body]
  `(binding [*format* ~format]
     ~@body))

(defmacro declare-section
  [name & opts]
  (let [name# name
        dispatch-name# (if (= (first opts) :seq)
                         "record-class-seq" "record-class" )
        dispatch-fn# (symbol dispatch-name#)
        serialization-dispatch# (symbol (str dispatch-name# "-serialization"))
        format-dispatch# (symbol (str dispatch-name# "-format"))
        serialization-name# (symbol (str name# "-serialization"))
        format-name# (symbol (str name# "-format"))
        type-name# (symbol (str name# "-type"))]
    `(do
       (defmulti ~serialization-name# ~serialization-dispatch#)
       (defmulti ~format-name# ~format-dispatch#)
       (defmulti ~type-name# ~dispatch-fn#)

       (defn ~name#
         [record# & options#]
         ;; (println "name: " (str ~name#))
         (if-let [format# (nth options# 0 *format*)]
           (if-let [serialization# (nth options# 1 *serialization*)]
             (let [opts# (apply vector format# serialization#
                                (drop 2 options#))]
               ;; (println "record: " record#)
               ;; (println "options# " opts#)
               ;; (println "")
               (apply ~serialization-name# record# opts#))
             (throw (IllegalArgumentException.
               "serialization not provided and *serialization* not set")))
           (throw (IllegalArgumentException.
             "format not provided and *format* not set"))))

       (defmethod ~serialization-name# :default
         [record# & others#]
         (apply ~format-name# record# others#))

       (defmethod ~format-name# :default
         [record# & others#]
         (apply ~type-name# record# others#)))))

(defmacro defsection
  [name dispatch-val binding-form & body]
  (let [name# name
        dispatch-val# dispatch-val
        type-name# (symbol (str name# "-type"))
        format-name# (symbol (str name# "-format"))
        serialization-name# (symbol (str name# "-serialization"))]
    (let [method-name#
          (if (= dispatch-val# :default)
            type-name#
            (condp = (count dispatch-val#)
                3 serialization-name#
                2 format-name#
                type-name#))]
      `(defmethod ~method-name# ~dispatch-val#
         ~binding-form ~@body))))

