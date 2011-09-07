(ns ciste.sections-test
  (:use (ciste test-helper sections)
        clojure.test))

(use-fixtures :each test-environment-fixture)

(defrecord User [])

(deftest record-class-serialization-test
  (testing "should return the parameters in order"
    (let [record (User.)
          format :html
          serialization :http]
      (is
       (= (record-class-serialization record format serialization)
          [User :html :http])))))
