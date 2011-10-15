(defproject ciste "0.2.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :repositories {"jiksnu-internal" "http://build.jiksnu.com/repository/internal"
                 "jiksnu-snapshots" "http://build.jiksnu.com/repository/snapshots"}
  :dependencies [[clj-factory "0.2.0-SNAPSHOT"]
                 [compojure "0.6.5"]
                 [hiccup "0.3.7"]
                 [inflections "0.5.3-SNAPSHOT"]
                 [midje "1.3-alpha3"]
                 [net.kronkltd/lamina "0.4.0-beta3-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.0"]
                 [org.clojure/data.xml "0.0.1-SNAPSHOT"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.slf4j/slf4j-simple "1.6.1"]]
  :exclusions [org.clojure/contrib
               org.clojure/clojure-contrib
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-jdk14]
  :warn-on-reflection false
  :jvm-opts ["-server"])
