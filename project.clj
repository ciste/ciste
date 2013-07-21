(defproject ciste/ciste "0.4.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[clj-factory "0.2.1"]
                 [clj-http "0.7.5"]
                 [compojure "1.1.5"]
                 [enlive "1.1.1"]
                 [inflections "0.8.1"]
                 [lamina "0.5.0-rc4"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/core.incubator "0.1.2"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/tools.logging "0.2.6"]
                 [xom "1.2.5"]]
  :profiles {:dev
             {:dependencies
              [[log4j "1.2.17"]
               [midje "1.6-alpha2"]]}}
  :plugins [[lein-midje "2.0.3"]
            [codox "0.6.4"]]
  :autodoc {:name "Ciste"
            :copyright "2012 KRONK Ltd."})
