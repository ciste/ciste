(defproject ciste "0.3.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :dependencies [[ciste/ciste-core "0.3.0-SNAPSHOT"]
                 [ciste/ciste-service-aleph "0.3.0-SNAPSHOT"]
                 [ciste/ciste-service-slacker "0.3.0-SNAPSHOT"]
                 [ciste/ciste-service-swank "0.3.0-SNAPSHOT"]]
  :dev-dependencies [[lein-sub "0.1.2"]]
  :sub [
        "ciste-core"
        "ciste-service-aleph"
        "ciste-service-slacker"
        "ciste-service-swank"
        ]
  :autodoc {:name "Ciste"
            :copyright "2012 KRONK Ltd."}
  :warn-on-reflection false
  :jvm-opts ["-server"])
