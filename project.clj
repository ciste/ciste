(defproject ciste "0.4.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[ciste/ciste-core "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-slacker "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-swank "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-tigase "0.4.0-SNAPSHOT"]]
  :plugins [[lein-sub "0.1.2"]]
  :sub ["ciste-core"
        "ciste-service-aleph"
        "ciste-service-slacker"
        "ciste-service-swank"
        "ciste-service-tigase"]
  :autodoc {:name "Ciste"
            :copyright "2012 KRONK Ltd."}
  :warn-on-reflection false
  :jvm-opts ["-server"])
