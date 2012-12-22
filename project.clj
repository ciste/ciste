(defproject ciste/ciste "0.4.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[clj-factory "0.2.1"]
                 [clj-http "0.6.3"]
                 [compojure "1.1.3"]
                 [enlive "1.0.1"]
                 [inflections "0.7.3"]
                 [lamina "0.5.0-alpha3"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/core.incubator "0.1.2"]
                 [org.clojure/data.json "0.1.3"]
                 [org.clojure/tools.logging "0.2.4"]
                 [xom "1.2.5"]]
  :profiles {:dev
             {:dependencies
              [[log4j "1.2.17"]
               [midje "1.5-alpha3"]]}}
  :plugins [[lein-midje "2.0.3"]
            [codox "0.6.4"]]
  :autodoc {:name "Ciste"
            :copyright "2012 KRONK Ltd."})
