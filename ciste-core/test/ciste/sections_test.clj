(ns ciste.sections-test
  (:use ciste.sections
        ciste.test-helper
        midje.sweet))

(test-environment-fixture)

(defrecord User [])

(fact "record-class-serialization"
  (fact "should return the parameters in order"
    (let [record (User.)
          format :html
          serialization :http]
      (record-class-serialization record format serialization) => [User :html :http])))
