(ns hellhound.component
  (:require [hellhound.system.protocols :as protocols])
  (:import (clojure.lang PersistentArrayMap)))


(defprotocol IComponent
  "This protocol defines a very basic component for hellhound system."
  (start! [component context]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started? [component]
    "Returns a `true` if component started and `false` otherwise.")

  (get-name [component]
    "Returns the name of the component.")

  (dependencies [component]
    "Returns a vector of dependency names."))

(extend-type PersistentArrayMap
  IComponent
  (start! [this context]
    (let [start-fn (::start-fn this)]
      (assoc (start-fn this context)
             ::started? true)))
  (stop! [this]
    (let [stop-fn (::stop-fn this)]
      (assoc (stop-fn this) ::started? false)))

  (started? [this]
    (or (::started? this) false))

  (get-name [this]
    (::name this))

  (dependencies [this]
    (::depends-on this)))
