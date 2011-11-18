(defproject ciste "0.2.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[clj-factory "0.2.0-SNAPSHOT"]
                 [clj-stacktrace "0.2.3"]
                 [compojure "0.6.5"]
                 [inflections "0.6.3-SNAPSHOT"]
                 [lamina "0.4.1-SNAPSHOT"]
                 [midje "1.3-alpha4"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [potemkin "0.1.0"]]
  :warn-on-reflection false
  :jvm-opts ["-server"])
