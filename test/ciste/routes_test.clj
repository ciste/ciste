(ns ciste.routes-test
  (:require [ciste.routes :refer [try-predicate]]
            [ciste.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer :all]))

(test-environment-fixture

 (facts "#'ciste.routes/try-predicate"

   (fact "when the predicate is a function"
     (let [invoked? (ref false)
           request {:method :get :path "/"}
           matcher {:method :get :path "/"}
           predicate (fn [request matcher]
                       (dosync (ref-set invoked? true))
                       request)]
       (try-predicate request matcher predicate)
       @invoked? => truthy))

   (fact "when the predicate is a sequence"
     (fact "and the first predicate fails"
       (let [invoked? (ref false)
             request {:method :get :path "/"}
             matcher {:method :get :path "/"}
             first-pred (fn [request matcher] nil)
             second-pred (fn [request matcher]
                           (dosync (ref-set invoked? true))
                           request)
             predicate [first-pred second-pred]]
         (try-predicate request matcher predicate)
         @invoked? => falsey)))

   (fact "when the first predicate returns a request"
     (let [request {:method :get :path "/"}
           request2 {:method :get :path "/foo"}
           matcher {:method :get :path "/"}]
       (letfn [(first-pred [_ _] request2)
               (second-pred [r _] r)]
         (let [predicate [first-pred second-pred]
               response (try-predicate request matcher first-pred)]
           response => request2)))))

 )
