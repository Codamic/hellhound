(defproject codamic/hellhound "0.13.0-SNAPSHOT"
  :description "A simple full-stack web framework for clojure"
  :license     {"GPLv3"
                "https://www.gnu.org/licenses/gpl.html"}
  :url         "http://github.com/Codamic/hellhound"
  :scm         {:url "https://github.com/Codamic/hellhound"}

  :dependencies [[org.clojure/clojure        "1.9.0-alpha14"]
                 [org.clojure/clojurescript  "1.9.521"]
                 [org.clojure/core.async     "0.3.442"]

                 [bidi                       "2.0.17"]
                 [reagent                    "0.6.1"]
                 [ring                       "1.6.0"]
                 [ring/ring-defaults         "0.3.0-beta1"]
                 [re-frame                   "0.9.2"]
                 [secretary                  "1.2.3"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.taoensso/tempura       "1.1.2"]
                 [codamic/sente              "1.11.1"]
                 [com.taoensso/timbre        "4.10.0"]
                 [org.danielsz/system        "0.4.0"]
                 [environ                    "1.1.0"]
                 [environ                    "1.1.0"]
                 [com.cemerick/friend        "0.2.3"]
                 [ring-logger                "0.7.7"]
                 [bk/ring-gzip               "0.2.1"]
                 [org.immutant/immutant      "2.1.6"
                  :exclusions [ch.qos.logback/logback-classic]]
                 [com.fzakaria/slf4j-timbre  "0.3.5"]
                 [potemkin                   "0.4.3"]
                 [com.cognitect/transit-clj  "0.8.300"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [colorize                   "0.1.1"
                  :exclusions [org.clojure/clojure]]
                 [ring/ring-anti-forgery     "1.1.0-beta1"]
                 [clj-http                   "3.5.0"]
                 ;;TODO: Move this to dev profile
                 [re-frisk                   "0.4.5"]
                 [selmer                     "1.10.7"]
                 [cheshire                   "5.7.1"]
                 [cljsjs/jquery              "2.2.4-0"]

                 ;; Cljs repl dependencies ----------------------------
                 ]

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
              :aot :all}})
