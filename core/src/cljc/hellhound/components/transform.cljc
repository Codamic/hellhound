(ns hellhound.components.transform
  "`transform` component is a really simple component to transofrm
  the incoming data and send the result to the output stream."
  ^{:author "Sameer Rahmani (@lxsameer)"}
  (:require
   [hellhound.stream     :as s]
   [hellhound.component :as hcomp]))


(defn- transform-fn
  [f]
  (fn [component context]
    (let [input  (hcomp/input component)
          output (hcomp/output component)]
      (s/connect-via input
                     #(s/put! output (f context %))
                     output))
    component))


(defn factory
  "Creates a component with the given `component-name` which transforms
  the input value by applying `f` to it and sending it to the output stream."
  [component-name f]
  {::hcomp/name     component-name
   ::hcomp/start-fn (transform-fn f)
   ::hcomp/stop-fn  (fn [component] component)})
