(ns ciste.predicates-test
  (:use [ciste.predicates :only [name-matches?]]
        [ciste.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [contains fact =>]]))

(test-environment-fixture

 (fact "name-matches?"
   (name-matches?
    {:name "foo"} {:name "foo"}) => (contains {:name "foo"})))
