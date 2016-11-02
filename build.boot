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
                 [adzerk/boot-cljs           "1.7.228-2" :scope "test"]
                 [binaryage/devtools         "0.8.2"     :scope "test"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/tempura       "1.0.0-RC3"]
                 [com.taoensso/sente         "1.11.0"]])

(task-options!
 pom {:project 'codamic/hellhound
      :version "0.7.0-SNAPSHOT"}
 jar {:manifest {"Description" "A simple full-stack web framework for clojure"
                 "Url" "http://github.com/Codamic/hellhound"}})

(deftask build
  "Build and install the hellhound"
  []
  (comp (pom) (jar) (install)))

(require '[adzerk.boot-cljs :refer [cljs]])
