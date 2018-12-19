(defproject codamic/hellhound.redis "1.0.0-SNAPSHOT"
  :description "Redis component for HellHound"
  :license     {"mit"
                "https://opensource.org/licenses/MIT"}
  :url         "http://hellhound.io"
  :scm         {:name "git"
                :url "https://github.com/Codamic/hellhound"}

  :dependencies [[codamic/hellhound.core "1.0.0-SNAPSHOT"]
                 [com.taoensso/carmine   "2.19.0"]]

  :plugins [[lein-codox "0.10.3"]]

  :min-lein-version "2.6.1"
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :clean-targets ^{:protect false} [:target-path]
  :uberjar-name "hellhound.redis.standalone.jar"
  :jar-name "hellhound.redis.jar"

  :profiles {:dev
             {:dependencies [[funcool/codeina "0.5.0"]]}
             :uberjar
             {:source-paths ^:replace ["src/clj"]
              :prep-tasks ["compile"]
              :hooks []
              :omit-source true
              :aot :all}
             :test {}})
