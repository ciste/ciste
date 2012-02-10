(defproject ciste "0.3.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[aleph "0.2.1-alpha1"]
                 [clj-factory "0.2.0"]
                 [clj-stacktrace "0.2.4"]
                 [compojure "1.0.1"]
                 [inflections "0.6.5"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.logging "0.2.3"]]
  :dev-dependencies [[lein-midje "1.0.8"]
                     [log4j "1.2.16"]
                     [midje "1.3.2-alpha1" :exclusions [org.clojure/clojure]]]
  :autodoc {:copyright "2012 KRONK Ltd."}
  :warn-on-reflection false
  :jvm-opts ["-server"])
