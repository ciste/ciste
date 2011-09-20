(ns ciste.config-test
  (:use ciste.config
        ciste.test-helper
        clojure.test
        midje.sweet))

(use-fixtures :each test-environment-fixture)

(deftest test-merge-config
  (fact
    (let [m1 {:key1 "value1"
              :key2 {:sub-key1 "value2"
                     :sub-key2 "value3"}}
          m2 {:key2 {:sub-key2 "value4"
                     :sub-key3 "value5"}
              :key3 "value6"}
          result (merge-config m1 m2)]
      result => (contains {:key1 "value1"})
      result => (contains {:key3 "value6"})
      (let [key2 (:key2 result)]
        key2 => (contains {:sub-key1 "value2"})
        key2 => (contains {:sub-key2 "value4"})
        key2 => (contains {:sub-key3 "value5"})))))
