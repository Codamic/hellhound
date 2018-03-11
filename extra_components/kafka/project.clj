(defproject codamic/hellhound.kafka "1.0.0-SNAPSHOT"
  :description "Kafka component for HellHound"
  :license     {"mit"
                "https://opensource.org/licenses/MIT"}
  :url         "http://hellhound.io"
  :scm         {:name "git"
                :url "https://github.com/Codamic/hellhound"}

  :dependencies [[org.apache.kafka/kafka-clients "1.0.1"]
                 [org.apache.kafka/kafka-streams "1.0.1"]]


  :plugins [[lein-codox "0.10.3"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]

  :test-paths ["test/clj"]
  :clean-targets ^{:protect false} [:target-path]

  :uberjar-name "hellhound.kafka.standalone.jar"
  :jar-name "hellhound.kafka.jar"

  :profiles {:dev
             {:dependencies [[funcool/codeina            "0.5.0"]
                             [org.clojure/tools.nrepl    "0.2.13"]]}

             :uberjar
             {:source-paths ^:replace ["src/clj"]
              :prep-tasks ["compile"]
              :hooks []
              :omit-source true
              :aot :all}

             :test {}})
