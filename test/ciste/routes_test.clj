(ns ciste.routes-test
  (:use (ciste test-helper routes)
        clojure.test
        midje.sweet))

(use-fixtures :each test-environment-fixture)

(deftest test-try-predicate
  (testing "when the predicate is a function"
    (fact "should apply that function"

      (let [invoked? (ref false)
            request {:method :get :path "/"}
            matcher {:method :get :path "/"}
            predicate (fn [request matcher]
                        (dosync (ref-set invoked? true))
                        request)]
        (try-predicate request matcher predicate)
        @invoked? => truthy)))
  (testing "when the predicate is a sequence"
    (testing "and the first predicate fails"
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
  (testing "when the first predicate returns a request"
    (fact "should pass that result to the next predicate"
      (let [request {:method :get :path "/"}
            request2 {:method :get :path "/foo"}
            matcher {:method :get :path "/"}]
        (letfn [(first-pred [_ _] request2)
                (second-pred [r _] r)]
          (let [predicate [first-pred second-pred]
                response (try-predicate request matcher first-pred)]
            response => request2))))))
