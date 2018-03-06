(defproject codamic/hellhound.core "1.0.0-SNAPSHOT"
  :description "The HellHound core is a library to create simple and elegant data pipelines."
  :license     {"mit"
                "https://opensource.org/licenses/MIT"}
  :url         "http://hellhound.io"
  :scm         {:name "git"
                :url "https://github.com/Codamic/hellhound"}

  :exclusions [ch.qos.logback/logback-classic]

  :dependencies [[org.clojure/clojure        "1.9.0"]
                 [org.clojure/test.check     "0.10.0-alpha2"]
                 ;; Logging
                 [org.slf4j/slf4j-api              "1.7.25"]
                 [com.taoensso/timbre              "4.10.0"]
                 [com.fzakaria/slf4j-timbre        "0.3.7"]
                 [org.slf4j/log4j-over-slf4j       "1.7.25"]
                 [org.slf4j/jul-to-slf4j           "1.7.25"]
                 [org.slf4j/jcl-over-slf4j         "1.7.25"]
                 [manifold                         "0.1.6"]]

  :plugins [[lein-codox "0.10.3"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]
  :clean-targets ^{:protect false} [:target-path]

  :uberjar-name "hellhound.core.standalone.jar"
  :jar-name "hellhound.core.jar"

  :repl-options {:init-ns user}

  :profiles {:dev
             {:dependencies [[funcool/codeina            "0.5.0"]
                             [hawk                       "0.2.11"]
                             [org.clojure/tools.nrepl    "0.2.13"]]}

             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile"]
              :hooks []
              :omit-source true
              :aot :all}

             :test {}})
