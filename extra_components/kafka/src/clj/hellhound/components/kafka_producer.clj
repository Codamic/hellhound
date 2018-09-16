(ns hellhound.components.kafka-producer
  (:require
   [hellhound.kafka.producers :as producers]
   [hellhound.component :as com]))

(defn start-fn
  [config]
  (fn [component context]
    (assoc component
           :producer (producers/make-producer config))))

(defn stop
  [this]
  (if-let [p (:producer this)]
    (producers/close p)))
