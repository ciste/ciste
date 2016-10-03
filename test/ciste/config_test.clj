(ns ciste.config-test
  (:require [ciste.config :refer [get-host-address merge-config]]
            [midje.sweet :refer [contains fact =>]]))

(fact "#'ciste.config/get-host-address"
  (fact "should return a string"
    (get-host-address) => string?))

(fact "#'ciste.config/merge-config"
  (let [m1 {:key1 "value1"
            :key2 {:sub-key1 "value2"
                   :sub-key2 "value3"}}
        m2 {:key2 {:sub-key2 "value4"
                   :sub-key3 "value5"}
            :key3 "value6"}
        result (merge-config m1 m2)]
    result => (contains {:key1 "value1"
                         :key3 "value6"})
    (let [key2 (:key2 result)]
      key2 => (contains {:sub-key1 "value2"
                         :sub-key2 "value4"
                         :sub-key3 "value5"}))))
