(ns ciste.test-helper
  (:use ciste.config
        midje.sweet))

(defmacro test-environment-fixture
  []
  `(background
    (around :facts
      (do (load-config)
          (with-environment :test
            ?form)))))
