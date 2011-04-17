(ns ciste.sections-test
  (:use ciste.sections
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(defrecord User [])

(describe record-class-serialization
  (do-it "should return the parameters in order"
    (let [record (User.)
          format :html
          serialization :http]
      (expect
       (= (record-class-serialization record format serialization)
          [User :html :http])))))
