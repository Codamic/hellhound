(ns hellhound.boot.helpers
  (:require [boot.core              :as core :refer [deftask]]
            [adzerk.boot-cljs       :refer [cljs]]
            [adzerk.boot-reload     :refer [reload]]
            [adzerk.boot-cljs-repl  :refer [cljs-repl start-repl]]
            [deraen.boot-less       :refer [less]]
            [deraen.boot-sass       :refer [sass]]
            [system.boot            :refer [system]]
            [boot.task.built-in     :refer :all]))


(defn dev-profile
  "Setup the development environment."
  [system]
  (core/set-env! :source-paths #(conj % "src/js/dev"))
  (core/task-options!
   cljs   {:optimizations :none :source-map true}
   system {:sys system :auto true}))

(deftask prod-profile
  "Setup the prod environment."
  []
  (core/set-env! :source-paths #(conj % "src/js/prod"))
  (core/task-options!
   cljs   {:optimizations :advanced}
   less   {:compression true}
   ;reload {:on-jsload 'sd.app/init}
   sass   {:compression true})
  identity)

(deftask build-frontend
  "Build the clojurescript application."
  []
  (comp (speak)
        (sass)
        (less)
        (cljs)
        (target)))

(deftask build-backend
  "Build and install the hellhound"
  []
  (comp (pom) (jar) (install)))


(deftask run
  "Run the application for respected environment. e.g boot dev run"
  []
  (comp (speak)
        (sass)
        (less)
        (watch)
        (reload)
        (cljs-repl)
        (cljs)
        (system)
        (target)))
