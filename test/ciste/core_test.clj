(ns ciste.core-test
  (:use clojure.test
        midje.sweet
        ciste.core
        ciste.test-helper))

(test-environment-fixture)

(fact "with-serialization"
  *serialization* => nil
  (with-serialization :http
    *serialization* => :http)
  *serialization* => nil)

(fact "with-format"
  *format* => nil
  (with-format :html
    *format* => :html)
  *format* => nil)

(fact "defaction"
  (fact "should define the var"
    (defaction foo
      []
      true)

    (foo) => true))
