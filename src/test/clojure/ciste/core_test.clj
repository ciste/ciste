(ns ciste.core-test
  (:use ciste.core
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe defaction)

(describe defview)

(describe lazier)

(describe default-format)

(describe apply-view)

(describe format-as)

(describe serialize-as)

(describe make-matchers)

(describe try-matcher
  (testing "when the predicate is a function"
    (do-it "should apply that function"
      (let [invoked? (ref false)
            request {:method :get :path "/"}
            matcher {:method :get :path "/"}]
        (letfn [(predicate [request matcher]
                        (dosync (ref-set invoked? true))
                  request)]
          (try-matcher request predicate matcher)
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
              (try-matcher request predicate matcher)
              (expect (false? @invoked?))))))))
  (testing "when the first predicate returns a request"
    (do-it "should pass that result to the next predicate"
      (let [request {:method :get :path "/"}
            request2 {:method :get :path "/foo"}
            matcher {:method :get :path "/"}]
        (letfn [(first-pred [_ _] request2)
                (second-pred [r _] r)]
          (let [predicate [first-pred second-pred]]
            (let [response (try-matcher request first-pred matcher)]
              (expect (= response request2)))))))))

(describe apply-template)

(describe try-matchers)

(describe resolve-route)

(describe resolve-routes)

