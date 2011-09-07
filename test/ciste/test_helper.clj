(ns ciste.test-helper
  (:use ciste.config))

(defn test-environment-fixture
  [f]
  (load-config)
  (with-environment :test
    (f)))
