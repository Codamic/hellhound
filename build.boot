(set-env!
 :resource-paths #{"src/clj"
                   "src/cljs"
                   "src/cljc"}
 :checkouts '[;[cljsjs/grommet                "1.1.0-0"]
              [codamic/sente                 "1.11.1"]]

 :dependencies '[[org.clojure/clojure        "1.9.0-alpha14"]
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
                 [com.cemerick/friend        "0.2.3"]
                 [ring-logger                "0.7.6"]
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

                 [selmer                     "1.0.9"]
                 [cheshire                   "5.7.0"]
                 [cljsjs/jquery              "2.2.4-0"]

                 ;; Testing tasks
                 [adzerk/boot-test            "1.2.0"          :scope "test"]
                 [crisptrutski/boot-cljs-test "0.3.0"          :scope "test"]

                 ;; Cljs repl dependencies ----------------------------
                 [adzerk/boot-cljs           "1.7.228-2"      :scope "test"]
                 [adzerk/boot-cljs-repl      "0.3.3"          :scope "test"]
                 [com.cemerick/piggieback    "0.2.1"          :scope "test"]
                 [weasel                     "0.7.0"          :scope "test"]
                 [org.clojure/tools.nrepl    "0.2.12"         :scope "test"]
                 [deraen/boot-less           "0.6.0"          :scope "test"]
                 [deraen/boot-sass           "0.3.0"          :scope "test"]
                 [adzerk/boot-reload         "0.4.13"         :scope "test"]

                 ;; Source code analyzer
                 [tolitius/boot-check        "0.1.4"          :scope "test"]

                 ;; ---------------------------------------------------
                 [funcool/codeina            "0.5.0"          :scope "test"]
                 [codamic/boot-codeina       "0.2.0-SNAPSHOT" :scope "test"]])


(require '[funcool.boot-codeina            :refer [apidoc]]
         '[adzerk.boot-test                :as t]
         '[crisptrutski.boot-cljs-test     :as tjs]
         '[tolitius.boot-check             :as check])

(def VERSION       "0.12.0-SNAPSHOT")
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



(deftask test-clj
  "Run the clj tests."
  [c clojure    VERSION   str    "the version of Clojure for testing."
   n namespaces NAMESPACE #{sym} "The set of namespace symbols to run tests in."
   e exclusions NAMESPACE #{sym} "The set of namespace symbols to be excluded from test."
   f filters    EXPR      #{edn} "The set of expressions to use to filter namespaces."
   X exclude    REGEX     regex  "the filter for excluded namespaces"
   I include    REGEX     regex  "the filter for included namespaces"
   r requires   REQUIRES  #{sym} "Extra namespaces to pre-load into the pool of test pods for speed."
   s shutdown   FN        #{sym} "functions to be called prior to pod shutdown"
   S startup    FN        #{sym} "functions to be called at pod startup"
   j junit-output-to JUNITOUT str "The directory where a junit formatted report will be generated for each ns"]
  (set-env! :source-paths #{"test/clj"})
  (comp
   (t/test :clojure    clojure
           :namespaces namespaces
           :exclusions exclusions
           :filters    filters
           :exclude    exclude
           :include    include
           :requires   requires
           :shutdown   shutdown
           :startup    startup
           :junit-output-to junit-output-to)))

(deftask test-cljs
  "Run the cljs tests."
  [n namespaces NAMESPACE #{sym} "The set of namespace symbols to run tests in."
   e exclusions NAMESPACE #{sym} "The set of namespace symbols to be excluded from test."
   i ids        IDS       #{str} "the filter for included namespaces"
   c cljs-opts  OPTS      str    "Extra namespaces to pre-load into the pool of test pods for speed."
   o optimizations LEVEL  str    "functions to be called prior to pod shutdown"
   j js-env     env        str "The directory where a junit formatted report will be generated for each ns"]
  (set-env! :source-paths #{"test/cljs"})
  (comp
   (tjs/test-cljs
           :namespaces namespaces
           :exclusions exclusions
           :ids        ids
           :js-env     js-env
           :cljs-opts  cljs-opts
           :optimizations optimizations)))

(deftask cljs-docs
  "Create the documents for cljs parts."
  []
  (apidoc :reader :clojurescript :target "doc/api/cljs" :source #{"src/cljs" "src/cljc"}))

(deftask clj-docs
  "Create the documents for clj parts."
  []
  (apidoc :source #{"src/clj" "src/cljc"}))

(deftask docs
  "Create all the docs."
  []
  (comp
   (cljs-docs)
   (clj-docs)))

(deftask build
  "Build and install the hellhound"
  []
  (comp (pom) (jar) (install)))

(deftask release
  "Build and release the snapshot version of hellhound"
  []
  (comp (docs) (build) (push)))

(deftask check-sources
  "Analyze the source tree and report."
  []
  (comp
    ;;(check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))


;; (deftask release-snapshot
;;   "Build and release the snapshot version of hellhound"
;;   []
;;   (comp (pom) (jar) (push-snapshot)))
