(ns ciste.sections-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections :only [record-class-serialization]]
        [ciste.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]]))

(defrecord User [])

(test-environment-fixture

 (fact "record-class-serialization"
   (let [record (User.)
         format :html
         serialization :http]
     (with-context [serialization format]
       (record-class-serialization record)) => [User format serialization])))
