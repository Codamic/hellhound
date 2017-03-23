(defproject codamic/hellhound "0.13.0-SNAPSHOT"
  :description "A simple full-stack web framework for clojure"
  :license     {"GPLv3"
                "https://www.gnu.org/licenses/gpl.html"}
  :url         "http://github.com/Codamic/hellhound"
  :scm         {:url "https://github.com/Codamic/hellhound"}

  :dependencies [[org.clojure/clojure        "1.9.0-alpha14"]
                 [org.clojure/clojurescript  "RELEASE"]
                 [bidi                       "2.0.14"]
                 [reagent                    "0.6.0"]
                 [ring                       "1.5.1"]
                 [ring/ring-defaults         "0.3.0-beta1"]
                 [re-frame                   "0.8.0"]
                 [secretary                  "1.2.3"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/tempura       "1.0.0-RC3"]
                 [codamic/sente              "1.11.1"]
                 [com.taoensso/timbre        "4.7.4"]
                 [org.danielsz/system        "0.3.2-SNAPSHOT"]
                 [environ                    "1.1.0"]
                 [environ                    "1.0.3"]
                 [com.cemerick/friend        "0.2.3"]
                 [ring-logger                "0.7.6"]
                 [bk/ring-gzip               "0.1.1"]
                 [org.immutant/immutant      "2.1.5"
                  :exclusions [ch.qos.logback/logback-classic]]
                 [com.fzakaria/slf4j-timbre  "0.3.2"]
                 [potemkin                   "0.4.3"]
                 [com.cognitect/transit-clj  "RELEASE"]
                 [com.cognitect/transit-cljs "RELEASE"]
                 [colorize                   "0.1.1"
                  :exclusions [org.clojure/clojure]]
                 [ring/ring-anti-forgery     "1.1.0-beta1"]
                 [clj-http                   "2.3.0"]
                 [re-frisk "0.3.2"]
                 [selmer                     "1.0.9"]
                 [cheshire                   "5.7.0"]
                 [cljsjs/jquery              "2.2.4-0"]

                 ;; Testing tasks


                 ;; Cljs repl dependencies ----------------------------
                 ;; ---------------------------------------------------
                 [funcool/codeina            "0.5.0"          :scope "test"]]

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
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc"]

                :figwheel true

                ;; Alternatively, you can configure a function to run every time figwheel reloads.
                ;; :figwheel {:on-jsload "hellhound.core/on-figwheel-reload"}

                :compiler {:main hellhound.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/hellhound.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "resources/public/js/compiled/testable.js"
                           :main hellhound.test-runner
                           :optimizations :none}}

               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main hellhound.core
                           :output-to "resources/public/js/compiled/hellhound.js"
                           :output-dir "target"
                           :source-map-timestamp true
                           :optimizations :advanced
                           :pretty-print false}}]}

  ;; When running figwheel from nREPL, figwheel will read this configuration
  ;; stanza, but it will read it without passing through leiningen's profile
  ;; merging. So don't put a :figwheel section under the :dev profile, it will
  ;; not be picked up, instead configure figwheel here on the top level.

  :figwheel {;; :http-server-root "public"       ;; serve static assets from resources/public/
             ;; :server-port 3449                ;; default
             ;; :server-ip "127.0.0.1"           ;; default
             :css-dirs ["resources/public/css"]  ;; watch and update CSS

             ;; Instead of booting a separate server on its own port, we embed
             ;; the server ring handler inside figwheel's http-kit server, so
             ;; assets and API endpoints can all be accessed on the same host
             ;; and port. If you prefer a separate server process then take this
             ;; out and start the server with `lein run`.
             :ring-handler user/http-handler

             ;; Start an nREPL server into the running figwheel process. We
             ;; don't do this, instead we do the opposite, running figwheel from
             ;; an nREPL process, see
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             ;; :nrepl-port 7888

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             :server-logfile "log/figwheel.log"}

  :doo {:build "test"}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.4-4"]
                             [figwheel-sidecar "0.5.4-4"]
                             [com.cemerick/piggieback "0.2.1"]
                             [org.clojure/tools.nrepl "0.2.12"]]

              :plugins [[lein-figwheel "0.5.4-4"]
                        [lein-doo "0.1.6"]]

              :source-paths ["dev"]
              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
              :hooks []
              :omit-source true
              :aot :all}})
