(ns ciste.routes-test
  (:use ciste.routes
        clojure.test))

(deftest try-predicate-test
  (testing "when the predicate is a function"
    (testing "should apply that function"
      (let [invoked? (ref false)
            request {:method :get :path "/"}
            matcher {:method :get :path "/"}]
        (letfn [(predicate [request matcher]
                        (dosync (ref-set invoked? true))
                  request)]
          (try-predicate request matcher predicate)
          (is (true? @invoked?))))))
  (testing "when the predicate is a sequence"
    (testing "and the first predicate fails"
      (testing "should not invoke the second"
        (let [invoked? (ref false)
              request {:method :get :path "/"}
              matcher {:method :get :path "/"}]
          (letfn [(first-pred [request matcher] nil)
                  (second-pred [request matcher]
                    (dosync (ref-set invoked? true))
                    request)]
            (let [predicate [first-pred second-pred]]
              (try-predicate request matcher predicate)
              (is (false? @invoked?))))))))
  (testing "when the first predicate returns a request"
    (testing "should pass that result to the next predicate"
      (let [request {:method :get :path "/"}
            request2 {:method :get :path "/foo"}
            matcher {:method :get :path "/"}]
        (letfn [(first-pred [_ _] request2)
                (second-pred [r _] r)]
          (let [predicate [first-pred second-pred]
                response (try-predicate request matcher first-pred)]
            (is (= response request2))))))))
