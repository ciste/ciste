(ns ciste.routes-test
  (:use ciste.routes
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe lazier)

(describe make-matchers)

(describe try-predicate
  (testing "when the predicate is a function"
    (do-it "should apply that function"
      (let [invoked? (ref false)
            request {:method :get :path "/"}
            matcher {:method :get :path "/"}]
        (letfn [(predicate [request matcher]
                        (dosync (ref-set invoked? true))
                  request)]
          (try-predicate request matcher predicate)
          (expect (true? @invoked?))))))
  (testing "when the predicate is a sequence"
    (testing "and the first predicate fails"
      (do-it "should not invoke the second"
        (let [invoked? (ref false)
              request {:method :get :path "/"}
              matcher {:method :get :path "/"}]
          (letfn [(first-pred [request matcher] nil)
                  (second-pred [request matcher]
                    (dosync (ref-set invoked? true))
                    request)]
            (let [predicate [first-pred second-pred]]
              (try-predicate request matcher predicate)
              (expect (false? @invoked?))))))))
  (testing "when the first predicate returns a request"
    (do-it "should pass that result to the next predicate"
      (let [request {:method :get :path "/"}
            request2 {:method :get :path "/foo"}
            matcher {:method :get :path "/"}]
        (letfn [(first-pred [_ _] request2)
                (second-pred [r _] r)]
          (let [predicate [first-pred second-pred]
                response (try-predicate request matcher first-pred)]
            (expect (= response request2))))))))

(describe try-predicates)

(describe invoke-action)

(describe resolve-route)

(describe resolve-routes)

