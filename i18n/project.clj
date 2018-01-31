(defproject codamic/hellhound.i18n "1.0.0-SNAPSHOT"
  :description "I18n library of HellHound."
  :license     {"mit"
                "https://opensource.org/licenses/MIT"}
  :url         "http://hellhound.io"
  :scm         {:url "https://github.com/Codamic/hellhound"}

  :dependencies [[com.taoensso/tempura "1.1.2"]]


  :plugins [[lein-codox "0.10.3"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/cljc"]

  :clean-targets ^{:protect false} [:target-path]

  :uberjar-name "hellhound.i18n.jar"

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (run) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}

  :profiles {:dev
             {:dependencies [[funcool/codeina            "0.5.0"]
                             [org.clojure/tools.nrepl    "0.2.13"]]}

             :uberjar
             {:source-paths ^:replace ["src/cljc"]
              :prep-tasks ["compile"]
              :hooks []
              :omit-source true
              :aot :all}

             :test {}})
