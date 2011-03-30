(ns ciste.factory-test
  (:use ciste.factory
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe get-counter
  (testing "when the type has been defined"
    (do-it "should return a number"
      (let [type :foo]
        (reset-counter! type)
        (let [response (get-counter type)]
          (expect (= 0 response)))))
    (testing "when it has been incremented"
      (do-it "should return a larger number"
        (let [type :foo]
          (reset-counter! type)
          (let [response1 (get-counter type)]
            (inc-counter! type)
            (let [response2 (get-counter type)]
              (expect (< response1 response2)))))))))

(describe inc-counter!)

(describe next-counter!)

(describe fseq)

(describe factory)

(describe deffactory)

(describe defseq)
