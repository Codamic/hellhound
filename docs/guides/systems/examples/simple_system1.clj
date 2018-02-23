(ns systems.simple-system1
  (:require [hellhound.system :as system]
            [hellhound.component :as hcomp]))

(defn start-fn1 <1>
  [component context]
  (println "Starting Component 1...")
  (assoc component :something-in "Hello World")) <2>

(defn stop-fn1 <3>
  [component]
  (println "Stopping component 2...")
  component)

(defn start-fn2 <4>
  [component context]
  (let [component1 (first (:dependencies context)) <5>
        component1-with-name (:simple-system/component-1 (:dependencies-map context))] <6>
    (println "Starting Component 2...")
    (println (:something-in component1)) <7>
    (println (:something-in component1-with-name))
    component))

(defn stop-fn2
  [component]
  (println "Stopping component 2...")
  component)

(def component-1 {:hellhound.component/name :simple-system/component-1 <8>
                  :hellhound.component/start-fn start-fn1
                  :hellhound.component/stop-fn  stop-fn1})

(def component-2 (hcomp/make-component :simple-system/component-2
                               start-fn2
                               stop-fn2
                               [:simple-system/component-1])) <9>

(def simple-system <10>
  {:components [component-2 component-1]})

(defn main
  []
  (system/set-system! simple-system) <11>
  (system/start!)) <12>
