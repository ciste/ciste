(ns ciste.sections.default
  (:use ciste.core
        ciste.sections
        (inflections [core :only (plural underscore)])))

(declare-section link-to)
(declare-section full-uri)
(declare-section uri)
(declare-section title)

(declare-section delete-button)
(declare-section add-form)
(declare-section edit-form)

(declare-section show-section)
(declare-section index-line)
(declare-section index-block :seq)
(declare-section index-section :seq)

(declare-section show-section-minimal)
(declare-section index-line-minimal)
(declare-section index-block-minimal :seq)
(declare-section index-section-minimal :seq)


(defsection title :default
  [record & options]
  "")

(defsection uri :default
  [record & options]
  (if-let [segment (if-let [model-name (class record)]
                     (plural (underscore (.getSimpleName model-name))))]
    (format "/%s/%s" segment (or (:_id record)
                                 (:id record)))))

(defsection show-section :default
  [record & options])

(defsection index-line :default
  [record & options]
  (show-section record))

(defsection index-block :default
  [records & options]
  (map index-line records))

(defsection index-section :default
  [records & options]
  (index-block records))

(defsection show-section-minimal :default
  [& _])
