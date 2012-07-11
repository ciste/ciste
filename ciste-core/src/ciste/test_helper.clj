(ns ciste.test-helper
  (:use [ciste.config :only [load-config with-environment]]
        [ciste.triggers :only [*thread-pool*]]))

(defmacro test-environment-fixture
  "Wrapper to ensure tests are run in the test environment"
  [& body]
  `(do
     (println " ")
     (println "****************************************************************************")
     (println (str "Testing " *ns*))
     (println "****************************************************************************")
     (println " ")
     (load-config)

     (with-environment :test
       ~@body
       (.shutdown @*thread-pool*))))
