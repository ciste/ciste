(defproject ciste/ciste "0.6.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[clj-factory "0.2.1"]
                 [clj-http "1.0.1"]
                 [compojure "1.3.1"]
                 [enlive "1.1.5"]
                 [inflections "0.9.13" :exclusions [commons-codec]]
                 [lamina "0.5.5"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [xom "1.2.5"]]
  :profiles {:dev
             {:dependencies
              [[log4j "1.2.17"]
               [midje "1.6.3"]
               [midje-junit-formatter "0.1.0-SNAPSHOT"]]}}
  :aot [ciste.runner]
  :plugins [[lein-midje "3.1.3"]
            [codox "0.6.7"]]
  :autodoc {:name "Ciste"
            :copyright "2014 KRONK Ltd."})
