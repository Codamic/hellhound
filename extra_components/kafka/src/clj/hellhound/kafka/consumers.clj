(ns hellhound.kafka.consumers
  (:require [hellhound.kafka.core :as core]))

(defn make-consumer [config]
  (let [props (core/make-config config)]
    (KafkaConsumer. props)))
