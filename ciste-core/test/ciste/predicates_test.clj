(ns ciste.predicates-test
  (:use (ciste [test-helper :only [test-environment-fixture]])
        ciste.predicates
        midje.sweet))

(test-environment-fixture

 (fact "name-matches?"
   (name-matches?
    {:name "foo"} {:name "foo"}) => (contains {:name "foo"})))
