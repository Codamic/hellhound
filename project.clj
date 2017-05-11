(defproject codamic/hellhound "0.14.0-SNAPSHOT"
  :description "A simple full-stack web framework for clojure"
  :license     {"GPLv3"
                "https://www.gnu.org/licenses/gpl.html"}
  :url         "http://github.com/Codamic/hellhound"
  :scm         {:url "https://github.com/Codamic/hellhound"}

  :dependencies [[org.clojure/clojure        "1.9.0-alpha14"]
                 [org.clojure/clojurescript  "1.9.521"]
                 [org.clojure/core.async     "0.3.442"]

                 ;; Database Migrations
                 [joplin.core                "0.3.10"]

                 ;; Bidirectional routing
                 [bidi                       "2.0.17"]

                 ;; Web Application spec
                 [ring                       "1.6.0"]
                 [ring/ring-defaults         "0.3.0-beta1"]
                 [ring-logger                "0.7.7"]
                 [bk/ring-gzip               "0.2.1"]
                 [ring/ring-anti-forgery     "1.1.0-beta1"]

                 ;; Communication
                 [codamic/sente              "1.11.1"]

                 ;; UI
                 [reagent                    "0.6.1"]
                 [re-frame                   "0.9.2"]
                 ;; Client Side routeing
                 [secretary                  "1.2.3"]
                 [re-frisk                   "0.4.5"]

                 ;; I18n
                 [com.taoensso/tempura       "1.1.2"]

                 ;; Configuration manager
                 [environ                    "1.1.0"]

                 ;; Transit support
                 [com.cognitect/transit-clj  "0.8.300"]
                 [com.cognitect/transit-cljs "0.8.239"]


                 [org.immutant/immutant      "2.1.6"
                  :exclusions [ch.qos.logback/logback-classic]]

                 ;; Misc
                 [colorize                   "0.1.1"
                  :exclusions [org.clojure/clojure]]

                 ;; Template Engine
                 [selmer                     "1.10.7"]

                 ;; JSON Parser
                 [cheshire                   "5.7.1"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-environ "1.0.3"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]
  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "hellhound.jar"

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (run) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}

  :cljsbuild {:builds
              [
               ;; {:id "app"
               ;;  :source-paths ["src/cljs" "src/cljc"]

               ;;  :figwheel true

               ;;  ;; Alternatively, you can configure a function to run every time figwheel reloads.
               ;;  ;; :figwheel {:on-jsload "hellhound.core/on-figwheel-reload"}

               ;;  :compiler {:main hellhound.core
               ;;             :asset-path "js/compiled/out"
               ;;             :output-to "resources/public/js/compiled/hellhound.js"
               ;;             :output-dir "resources/public/js/compiled/out"
               ;;             :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "resources/public/js/compiled/testable.js"
                           :main hellhound.test-runner
                           :optimizations :none}}

               {:id "optimized-test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "resources/public/js/compiled/testable.js"
                           :main hellhound.test-runner
                           :optimizations :advance}}

               ;; {:id "min"
               ;;  :source-paths ["src/cljs" "src/cljc"]
               ;;  :jar true
               ;;  :compiler {:main hellhound.core
               ;;             :output-to "resources/public/js/compiled/hellhound.js"
               ;;             :output-dir "target"
               ;;             :source-map-timestamp true
               ;;             :optimizations :advanced
               ;;             :pretty-print false}}
               ]}

  :doo {:build "test"}

  :profiles {:dev
             {:dependencies [[figwheel                   "0.5.10"]
                             [figwheel-sidecar           "0.5.10"]
                             [funcool/codeina            "0.5.0"]
                             [com.cemerick/piggieback    "0.2.1"]
                             [org.clojure/tools.nrepl    "0.2.13"]]

              :plugins [[lein-figwheel  "0.5.4-4"]
                        [lein-doo       "0.1.6"]]

              :source-paths ["dev"]
              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
              :hooks []
              :omit-source true
              :aot :all}

             :test {:dependencies [[clj-http "3.5.0"]]}})
