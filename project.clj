(defproject codamic/hell-hound "0.5.0-SNAPSHOT"
  :description "A simple full-stack web framework for clojure"
  :url "http://github.com/Codamic/hell-hound"
  :license {:name "GPLv3"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cljsjs/jquery "2.2.4-0"]
                 [compojure "1.5.0"]
                 [reagent "0.6.0"]
                 [ring "1.5.0"]
                 [binaryage/devtools "0.8.2"]
                 [re-frame "0.8.0"]
                 [secretary "1.2.3"]
                 [http-kit "2.1.18"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/tempura "1.0.0-RC3"]
                 [com.taoensso/sente "1.11.0"]]

  :plugins [[funcool/codeina "0.4.0"
             :exclusions [org.clojure/clojure]]]

  :prof3iles {:dev {}
             :clj {:codeina {:sources ["src"]
                             :reader :clojure
                             :target "doc/dist/latest/api"
                             :src-uri "http://github.com/Codamic/hell-hound/blob/master/"
                             :src-uri-prefix "#L" }}

             :cljs {:codeina {:sources ["src"]
                              :reader :clojurescript
                              :target "doc/dist/latest/fontend/api"
                              :src-uri "http://github.com/Codamic/hell-hound/blob/master/"
                              :src-uri-prefix "#L"}}})
