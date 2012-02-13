(ns ciste.predicates-test
  (:use ciste.predicates
        midje.sweet))

(fact "name-matches?"
  (name-matches?
   {:name "foo"} {:name "foo"}) => (contains {:name "foo"}))
