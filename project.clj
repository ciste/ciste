(defproject ciste "0.2.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :repositories {"jiksnu-internal" "http://build.jiksnu.com/repository/internal"
                 "jiksnu-snapshots" "http://build.jiksnu.com/repository/snapshots"}
  :dependencies [[compojure "0.6.5"]
                 [hiccup "0.3.6"]
                 [inflections "0.5.2"]
                 [org.clojure/clojure "1.3.0-beta3"]
                 [org.clojure/data.json "0.1.0"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.clojure.contrib/lazy-xml "1.3.0-alpha4"]
                 [net.kronkltd/lamina "0.4.0-beta2-SNAPSHOT"]]
  :dev-dependencies [[net.kronkltd/midje "1.3-alpha2-SNAPSHOT"]]
  :exclusions [org.clojure/contrib
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-jdk14]
  :warn-on-reflection false
  :jvm-opts ["-server"])
