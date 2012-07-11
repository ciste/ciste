(ns ciste.config-test
  (:use [ciste.config :only [environment get-host-address get-host-name merge-config]]
        [ciste.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [contains fact =>]]))

(test-environment-fixture
 
 (fact "get-host-name"
   (fact "should return a string"
     (get-host-name) => string?))

 (fact "get-host-address"
   (fact "should return a string"
     (get-host-address) => string?))

 (fact "environment"
   (environment) => :test)

 (fact "merge-config"
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
       key2 => (contains {:sub-key3 "value5"}))))

 )
