(ns systems.simple-system2
  (:require
   [manifold.stream :as s]
   [hellhound.system :as system]
   [hellhound.component :as hcomp]))

(defn start-fn1 <1>
  [component context]
  (let [input  (hcomp/input component)
        output (hcomp/output component)]

    (s/consume (fn [x]
                 (println "Message Component 1: " x)
                 (s/put! output (inc x)))
               input))
  component)

;; Stop function of all the components. It should returns a component
;; map.
(defn stop-fn
  [component]
  component)

;; Start function of component-2.
(defn start-fn2
  [component context]
  (let [input (hcomp/input component) <2>
        output (hcomp/output component)] <3>

    (s/connect-via input
                   (fn [x]
                     (println "Message Component 2: " x)
                     (s/put! output (inc x)))
                   output) <4>
    component))

;; Start function of component-2.
(defn start-fn3
  [component context]
  (let [input (hcomp/input component)]
    (s/consume #(println "Message Component 3: " %) input) <5>
    component))

(def component-1 (hcomp/make-component :simple-system/component-1 start-fn1 stop-fn)) <6>
(def component-2 (hcomp/make-component :simple-system/component-2 start-fn2 stop-fn))
(def component-3 (hcomp/make-component :simple-system/component-3 start-fn3 stop-fn))

(def simple-system
  {:components [component-2 component-1 component-3]
   :workflow [[:simple-system/component-1 :simple-system/component-2] <7>
              [:simple-system/component-2 :simple-system/component-3]]})

(defn main
  []
  (system/set-system! simple-system)
  (system/start!)


  (let [component1 (system/get-component :simple-system/component-1) <8>
        input      (hcomp/input component1)]

    (-> [1 2 3 4 5 6]
        (s/->source) <9>
        (s/connect input))) <10>
  (println "Done."))
