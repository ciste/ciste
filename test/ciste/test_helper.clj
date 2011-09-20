(ns ciste.test-helper
  (:use ciste.config))

(defn test-environment-fixture
  [f]
  (with-environment :test
    (f)))
