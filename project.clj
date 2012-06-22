(defproject ciste/ciste "0.6.0-SNAPSHOT"
  :description "MVC platform for Clojure applications"
  :url "http://github.com/duck1123/ciste"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :cljsbuild {:builds []}
  :dependencies [[clj-factory "0.2.1"]
                 [clj-http "3.4.1"]
                 [clojurewerkz/propertied "1.2.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [compojure "1.5.1"]
                 [enlive "1.1.6"]
                 [environ "1.1.0"]
                 [inflections "0.12.2" :exclusions [commons-codec]]
                 [manifold "0.1.5"]
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/data.json "0.2.6"]
                 [xom "1.2.5"]]
  :profiles {:dev {:dependencies [[midje "1.9.0-alpha6"]
                                  [org.slf4j/slf4j-log4j12 "1.7.22"]]}}
  :auto-clean false
  ;; :aot [ciste.runner]
  :plugins [[lein-midje "3.1.3"]
            [codox "0.8.11"]]
  :repositories [["snapshots" {:url "http://repo.jiksnu.org/repository/maven-snapshots/"
                               :username [:gpg :env/repo_username]
                               :password [:gpg :env/repo_password]}]
                 ["releases" {:url "http://repo.jiksnu.org/repository/maven-releases/"
                              :username [:gpg :env/repo_username]
                              :password [:gpg :env/repo_password]}]]
  :sub [
        "ciste-core"
        "ciste-service-aleph"
        "ciste-service-slacker"
        "ciste-service-swank"
        "ciste-service-tigase"
        ]
  :autodoc {:name "Ciste"
            :copyright "2014 KRONK Ltd."})
