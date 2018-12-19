(defproject codamic/hellhound.utils "1.0.0-SNAPSHOT"
  :description "The HellHound utils is a library containing some useful components."
  :license     {"mit"
                "https://opensource.org/licenses/MIT"}
  :url         "http://hellhound.io"
  :scm         {:name "git"
                :url "https://github.com/Codamic/hellhound"}

  :dependencies [[codamic/hellhound.core      "1.0.0-SNAPSHOT"]
  		 [refactor-nrepl              "2.4.0"]
                 [cider/cider-nrepl           "0.18.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/java.classpath  "0.3.0"]
                 [hawk                        "0.2.11"]]

  :plugins [[lein-codox "0.10.3"]]
  :min-lein-version "2.6.1"
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :clean-targets ^{:protect false} [:target-path]

  :uberjar-name "hellhound.utils.standalone.jar"
  :jar-name "hellhound.utils.jar"

  :profiles
  {:dev
   {:dependencies [[funcool/codeina "0.5.0"]]}
   :uberjar
   {:prep-tasks ["compile"]
    :hooks []
    :omit-source true
    :aot :all}
   :test {}})
