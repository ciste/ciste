(ns
    ^{:doc "Sections are a series of multimethods for generically transforming
records into the most appropriate format.

A Section dispatches on a Vector containing the type of the first
argument or the type of the first element of the first argument if the
Section has been defined as a :seq type, the Format, and
the Serialization. If no match is found, the final value is removed
and tried again. This repeats until there is only the type.

Example:

    (declare-section show-section)
    (declare-section index-section :seq)

    (defsection show-section [User :html :http]
      [user & options]
      [:div
        [:p \"Name: \" (:name user)]
        [:p \"Email: \" (:email user)]])

    (defsection index-section [User :html :http]
      [users & options]
      [:ul
        (map
          (fn [user]
            [:li (show-section user)])
          users)])
"
      }
    ciste.sections
  (:use (ciste [core :only [*format* *serialization*]]
               [debug :only [spy]])))

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

(defmacro declare-section
  "Setup a section with the given name"
  [name & opts]
  (let [name# name
        dispatch-name# (if (= (first opts) :seq)
                         "record-class-seq" "record-class" )

        ;; Find a way to make this automatic
        ;; One option would be to capture the ns outside the defmacro,
        ;; creating a closure. I'm not sure if that's bad practice, however.
        dispatch-ns# (the-ns 'ciste.sections)
        
        dispatch-fn# (ns-resolve dispatch-ns# (symbol dispatch-name#))
        serialization-dispatch# (ns-resolve dispatch-ns# (symbol (str dispatch-name# "-serialization")))
        format-dispatch# (ns-resolve dispatch-ns# (symbol (str dispatch-name# "-format")))

        serialization-name# (symbol (str name# "-serialization"))
        format-name# (symbol (str name# "-format"))
        type-name# (symbol (str name# "-type"))]
    `(do
       (defmulti ~serialization-name# ~serialization-dispatch#)
       (defmulti ~format-name# ~format-dispatch#)
       (defmulti ~type-name# ~dispatch-fn#)

       (defn ~name#
         [record# & options#]
         (if *format*
           (if *serialization*
             (apply ~serialization-name# record# options#)
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
  (let [name# name]
    (if-let [declared-ns# (-> name resolve meta :ns)]
     (let [dispatch-val# dispatch-val
           type-name# (symbol (str name# "-type"))
           format-name# (symbol (str name# "-format"))
           serialization-name# (symbol (str name# "-serialization"))
           method-name# (if (= dispatch-val# :default)
                          type-name#
                          (condp = (count dispatch-val#)
                            3 serialization-name#
                            2 format-name#
                            type-name#))
           full-symbol# (symbol (str declared-ns# "/" method-name#))]
       `(defmethod ~full-symbol# ~dispatch-val#
          ~binding-form ~@body))
     (throw (IllegalArgumentException. (str "Can not resolve section: " name))))))

