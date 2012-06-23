(defproject ciste/ciste-service-slacker "0.4.0-SNAPSHOT"
  :description "Slacker Connector Service for Ciste Applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[ciste/ciste-core "0.4.0-SNAPSHOT"]
                 [slacker "0.8.2"]]
  :profiles {:dev
             {:dependencies
              [[log4j "1.2.16"]
               [midje "1.4.0"]]}}
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]]
  :autodoc {:name "Ciste Service Slacker"
            :copyright "2012 KRONK Ltd."}
  :warn-on-reflection true
  :jvm-opts ["-server"])
