(ns ciste.sections-test
  (:use [ciste.sections :only [record-class-serialization]]
        [ciste.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]]))

(defrecord User [])

(test-environment-fixture

 (fact "record-class-serialization"
   (fact "should return the parameters in order"
     (let [record (User.)
           format :html
           serialization :http]
       (record-class-serialization record format serialization) => [User :html :http]))))
