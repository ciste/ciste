(defproject ciste/ciste-service-swank "0.4.0-SNAPSHOT"
  :description "Swank Connector Service for Ciste Applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[ciste/ciste-core "0.4.0-SNAPSHOT"]
                 [swank-clojure "1.4.0"]]
  :profiles {:dev
             {:dependencies
              [[log4j "1.2.16"]
               [midje "1.4.0"]]}}
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]]
  :autodoc {:name "Ciste Service Swank"
            :copyright "2012 KRONK Ltd."})
