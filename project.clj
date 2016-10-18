(defproject hell-hound "0.1.0-SNAPSHOT"
  :description "A simple full-stack web framework for clojure"
  :url "http://github.com/Codamic/hell-hound"
  :license {:name "GPLv3"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cljsjs/jquery "2.2.4-0"]
                 [com.taoensso/tempura "1.0.0-RC3"]]

  :plugins [[funcool/codeina "0.4.0"
             :exclusions [org.clojure/clojure]]]

  :profiles {:dev {}
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
