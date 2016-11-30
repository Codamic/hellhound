(set-env!
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojure        "1.9.0-alpha14"]
                 [cljsjs/jquery              "2.2.4-0"]
                 [bidi                       "2.0.14"]
                 [reagent                    "0.6.0"]
                 [ring                       "1.5.0"]
                 [ring/ring-anti-forgery     "1.0.1"]
                 [ring/ring-defaults         "0.3.0-beta1"]
                 [http-kit                   "2.1.18"]
                 [re-frame                   "0.8.0"]
                 [secretary                  "1.2.3"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/tempura       "1.0.0-RC3"]
                 [com.taoensso/sente         "1.11.0"]
                 [org.danielsz/system        "0.3.2-SNAPSHOT"]
                 [environ                    "1.1.0"]
                 [boot-environ               "1.1.0"]
                 [com.cemerick/friend        "0.2.3"]

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
                 [funcool/codeina            "0.5.0"          :scope "test"]
                 [codamic/boot-codeina       "0.2.0-SNAPSHOT" :scope "test"]])


(require '[funcool.boot-codeina :refer [apidoc]]
         '[taoensso.sente])

(def VERSION       "0.11.0-SNAPSHOT")
(def DESCRIPTION   "A simple full-stack web framework for clojure")


(task-options!
 pom {:project     'codamic/hellhound
      :version     VERSION
      :description DESCRIPTION
      :license     {"GPLv3"
                    "https://www.gnu.org/licenses/gpl.html"}
      :url         "http://github.com/Codamic/hellhound"
      :scm         {:url "https://github.com/Codamic/hellhound"}}

 jar {:manifest    {"Description" DESCRIPTION
                    "Url"         "http://github.com/Codamic/hellhound"}}

 apidoc            {:version     VERSION
                    :title       "HellHound"
                    :sources     #{"src"}
                    :reader      :clojure
                    :target      "doc/api/clj"
                    :description DESCRIPTION}
 push              {:repo "clojars"})


(deftask cljs-docs
  "Create the documents for cljs parts."
  []
  (apidoc :reader :clojurescript :target "doc/api/cljs"))

(deftask clj-docs
  "Create the documents for clj parts."
  []
  (apidoc))

(deftask docs
  "Create all the docs."
  []
  (comp
   (speak)
   (cljs-docs)
   (clj-docs)))

(deftask build
  "Build and install the hellhound"
  []
  (comp (pom) (jar) (install)))

(deftask release
  "Build and release the snapshot version of hellhound"
  []
  (comp (pom) (jar) (push)))

;; (deftask release-snapshot
;;   "Build and release the snapshot version of hellhound"
;;   []
;;   (comp (pom) (jar) (push-snapshot)))
