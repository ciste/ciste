(ns ciste.core-test
  (:use clojure.test
        midje.sweet
        ciste.core
        ciste.test-helper))

(test-environment-fixture)

(deftest test-defaction
  (fact "should define the var"
    (defaction foo
      []
      true)

    (foo) => true))
