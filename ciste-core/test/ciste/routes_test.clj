(ns ciste.routes-test
  (:use [ciste.routes :only [try-predicate]]
        [ciste.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact falsey truthy =>]]))

(test-environment-fixture

 (fact "when the predicate is a function"
   (fact "should apply that function"

     (let [invoked? (ref false)
           request {:method :get :path "/"}
           matcher {:method :get :path "/"}
           predicate (fn [request matcher]
                       (dosync (ref-set invoked? true))
                       request)]
       (try-predicate request matcher predicate)
       @invoked? => truthy)))
 (fact "when the predicate is a sequence"
   (fact "and the first predicate fails"
     (fact "should not invoke the second"
       (let [invoked? (ref false)
             request {:method :get :path "/"}
             matcher {:method :get :path "/"}
             first-pred (fn [request matcher] nil)
             second-pred (fn [request matcher]
                           (dosync (ref-set invoked? true))
                           request)
             predicate [first-pred second-pred]]
         (try-predicate request matcher predicate)
         @invoked? => falsey))))
 (fact "when the first predicate returns a request"
   (fact "should pass that result to the next predicate"
     (let [request {:method :get :path "/"}
           request2 {:method :get :path "/foo"}
           matcher {:method :get :path "/"}]
       (letfn [(first-pred [_ _] request2)
               (second-pred [r _] r)]
         (let [predicate [first-pred second-pred]
               response (try-predicate request matcher first-pred)]
           response => request2))))))
