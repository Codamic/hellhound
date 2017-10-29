(ns systems.simple-system3
  (:require
   [manifold.stream :as s]
   [hellhound.system :as system :refer [defcomponent]]
   [hellhound.component :as hcomp]))

(defn start-fn1
  [component context]
  (let [input  (hcomp/input component)
        output (hcomp/output component)]
    (s/connect input output)) <1>
  component)

(defn stop-fn
  [component]
  component)

(defn start-fn2
  [component context]
  (let [input (hcomp/input component)]
    (s/consume #(println "Odd: " %) input) <2>
    component))

;; Start function of component-2.
(defn start-fn3
  [component context]
  (let [input (hcomp/input component)]
    (s/consume #(println "Even: " %) input) <3>
    component))

(def component-1 (defcomponent :simple-system/component-1 start-fn1 stop-fn))
(def component-2 (defcomponent :simple-system/component-2 start-fn2 stop-fn))
(def component-3 (defcomponent :simple-system/component-3 start-fn3 stop-fn))

(def simple-system
  {:components [component-2 component-1 component-3]
   :workflow [[:simple-system/component-1 odd? :simple-system/component-2] <4>
              [:simple-system/component-1 even? :simple-system/component-3]]}) <5>

(defn main
  []
  (system/set-system! simple-system)
  (system/start!)

  ;; Gets a component with the name from the default system.
  (let [component1 (system/get-component :simple-system/component-1)
        input      (hcomp/input component1)]

    (-> [1 2 3 4 5 6]
        (s/->source)
        (s/connect input)))
  (println "Done."))
