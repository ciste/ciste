(ns ciste.views-test
  (:use ciste.views
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe defview)

(describe apply-view)

