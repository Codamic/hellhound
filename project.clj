(defproject codamic/hellhound "1.0.0-SNAPSHOT"
  :description "Build asynchronous, high performance and scalable applications at ease."
  :url "http://hellhound.io"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :scm         {:name "git"
                :url "https://github.com/Codamic/hellhound"}
  :dependencies [[org.clojure/clojure     "1.9.0"]
                 [codamic/hellhound.core  "1.0.0-SNAPSHOT"]
                 [codamic/hellhound.http  "1.0.0-SNAPSHOT"]]
                 ;;[codamic/hellhound.kafka "1.0.0-SNAPSHOT"]



  :plugins [[lein-sub "0.3.0"]
            [lein-codox "0.10.3"]
            [lein-kibit "0.1.6"]
            [jonase/eastwood "0.2.7"]
            [lein-bikeshed "0.5.1"]]

  :sub ["core" "i18n" "http"
        ;;"extra_components/kafka"
        ]


  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]

  :uberjar-name "hellhound.standalone.jar"
  :jar-name "hellhound.jar"

  :codox {:output-path "docs/api/"
          :metadata {:doc/format :markdown}
          :doc-paths ["docs/guides/"]
          :source-uri "http://github.com/Codamic/hellhound/blob/{version}/{filepath}#L{line}"
          :source-paths ["core/src"
                         "i18n/src"
                         "http/src"
                         "extra_components/kafka/src"]})
