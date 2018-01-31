(defproject codamic/hellhound "1.0.0-SNAPSHOT"
  :description "Build asynchronous, high performance and scalable applications at ease."
  :url "https://github.com/ring-clojure/ring"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure    "1.9.0"]
                 [codamic/hellhound.core "1.0.0-SNAPSHOT"]
                 [codamic/hellhound.http "1.0.0-SNAPSHOT"]]

  :plugins [[lein-sub "0.3.0"]
            [lein-codox "0.10.3"]]
  :sub ["core"
        "i18n"
        "http"]

  :codox {:output-path "docs/api/"
          :metadata {:doc/format :markdown}
          :doc-paths ["docs/guides/"]
          :source-uri "http://github.com/Codamic/hellhound/blob/{version}/{filepath}#L{line}"
          :source-paths ["core/src"
                         "i18n/src"
                         "http/src"]})
