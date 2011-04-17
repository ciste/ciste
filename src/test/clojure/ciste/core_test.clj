(ns ciste.core-test
  (:use ciste.core
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe defaction)

(describe default-format)

(describe serialize-as)

(describe apply-template)

