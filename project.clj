(defproject ciste/ciste-core "0.4.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
    :min-lein-version "2.0.0"
  :dependencies [[clj-factory "0.2.0"]
                 [clj-http "0.4.2"]
                 [compojure "1.0.1"]
                 [enlive "1.0.0"]
                 [inflections "0.6.5"]
                 [lamina "0.5.0-alpha3"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.logging "0.2.3"]
                 [xml-picker-seq "0.0.2"]]
  :profiles {:dev
             {:dependencies
              [[log4j "1.2.16"]
               [midje "1.4.0"]]}}
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]]
  :autodoc {:name "Ciste"
            :copyright "2012 KRONK Ltd."}
  :warn-on-reflection false
  :jvm-opts ["-server"]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
