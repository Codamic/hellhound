(set-env!
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojure        "1.8.0"]
                 [cljsjs/jquery              "2.2.4-0"]
                 [compojure                  "1.5.0"]
                 [reagent                    "0.6.0"]
                 [ring                       "1.5.0"]
                 [ring/ring-anti-forgery     "1.0.1"]
                 [http-kit                   "2.1.18"]
                 [re-frame                   "0.8.0"]
                 [secretary                  "1.2.3"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/tempura       "1.0.0-RC3"]
                 [com.taoensso/sente         "1.11.0"]
                 [org.danielsz/system        "0.3.2-SNAPSHOT"]

                 [adzerk/boot-cljs           "1.7.228-2"      :scope "test"]
                 [deraen/boot-less           "0.6.0"          :scope "test"]
                 [deraen/boot-sass           "0.3.0"          :scope "test"]
                 [binaryage/devtools         "0.8.2"          :scope "test"]

                 [adzerk/boot-reload         "0.4.13"         :scope "test"]

                 ;; Cljs repl dependencies ----------------------------
                 [adzerk/boot-cljs-repl      "0.3.3"          :scope "test"]
                 [com.cemerick/piggieback    "0.2.1"          :scope "test"]
                 [weasel                     "0.7.0"          :scope "test"]
                 [org.clojure/tools.nrepl    "0.2.12"         :scope "test"]
                 ;; ---------------------------------------------------
                 [funcool/boot-codeina       "0.1.0-SNAPSHOT" :scope "test"]])

(def VERSION       "0.8.0-SNAPSHOT")
(def DESCRIPTION   "A simple full-stack web framework for clojure")
(task-options!
 pom {:project     'codamic/hellhound
      :version     VERSION
      :description DESCRIPTION
      :license     {:name "GPLv3"
                    :url  "https://www.gnu.org/licenses/gpl.html"}
      :url         "http://github.com/Codamic/hellhound"}

 jar {:manifest    {"Description" DESCRIPTION
                    "Url"         "http://github.com/Codamic/hellhound"}}

 apidoc            {:version VERSION
                    :title   "HellHound"
                    :sources #{"src"}
                    :format  :markdown
                    :target  "doc"
                    :description DESCRIPTION})

(require '[funcool.boot-codeina :refer :all])

(deftask build
  "Build and install the hellhound"
  []
  (comp (pom) (jar) (install)))

(require '[adzerk.boot-cljs :refer [cljs]])
