(ns ciste.triggers-test
  (:use ciste.triggers
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe add-trigger!)

(describe make-trigger)

(describe run-triggers)
