(defproject ciste/ciste "0.6.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[clj-factory "0.2.1"]
                 [clj-http "3.3.0"]
                 [clojurewerkz/propertied "1.2.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [compojure "1.5.1"]
                 [enlive "1.1.6"]
                 [environ "1.1.0"]
                 [inflections "0.12.2" :exclusions [commons-codec]]
                 [manifold "0.1.5"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [xom "1.2.5"]]
  :profiles {:dev {:dependencies [[log4j "1.2.17"]
                                  [midje "1.8.3"]
                                  [org.slf4j/slf4j-log4j12 "1.7.21"]]}}
  :auto-clean false
  ;; :aot [ciste.runner]
  :plugins [[lein-midje "3.1.3"]
            [codox "0.8.11"]]
  :pom-plugins [[org.apache.maven.plugins/maven-jar-plugin "2.6"
                 {:executions ([:execution
                                [:id "default-jar"]
                                [:phase "never"]
                                [:configuration
                                 [:finalName "unwanted"]
                                 [:classifier "unwanted"]]])}]
                [org.codehaus.mojo/build-helper-maven-plugin "1.10"
                 {:executions ([:execution
                                [:id "attach-artifacts"]
                                [:phase "package"]
                                [:goals
                                 [:goal "attach-artifact"]
                                 ]
                                [:configuration
                                 [:artifacts
                                  [:artifact
                                   [:file "target/ciste-0.6.0-SNAPSHOT.jar"]
                                   [:type "jar"]
                                   [:classifier "compile"]]]]])}]]
  :pom-addition ([:properties
                  [:project.build.sourceEncoding "UTF-8"]])
  :repositories [["snapshots" {:url "http://repo.jiksnu.org/repository/maven-snapshots/"
                               :username [:gpg :env/repo_username]
                               :password [:gpg :env/repo_password]}]
                 ["releases" {:url "http://repo.jiksnu.org/repository/maven-releases/"
                              :username [:gpg :env/repo_username]
                              :password [:gpg :env/repo_password]}]]
  :autodoc {:name "Ciste"
            :copyright "2014 KRONK Ltd."})
