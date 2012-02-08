(defproject ciste "0.2.0"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[clj-factory "0.2.0"]
                 [clj-stacktrace "0.2.3"]
                 [compojure "0.6.5"]
                 [inflections "0.6.3"]
                 [lamina "0.4.1-alpha1"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.logging "0.2.3"]]
  :dev-dependencies [[lein-midje "1.0.7"]
                     [log4j "1.2.16"]
                     [midje "1.3.0" :exclusions [org.clojure/clojure]]]
  :warn-on-reflection false
  :jvm-opts ["-server"])
