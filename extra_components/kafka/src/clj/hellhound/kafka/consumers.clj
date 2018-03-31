(ns hellhound.kafka.consumers
  (:require
   [hellhound.kafka.core :as core])
  (:import
   [org.apache.kafka.clients.consumer KafkaConsumer]
   [org.apache.kafka.common.serialization StringDeserializer]))


(def default-key-serializer (StringDeserializer.))
(def default-value-deserializer (StringDeserializer.))

(defn make-consumer
  ([config]
   (make-consumer config
                  default-key-serializer
                  default-value-deserializer))

  ([config key-deserializer value-deserializer]
   (let [props (core/make-config config)]
     (KafkaConsumer. props key-deserializer value-deserializer))))

(defn subscribe
  [consumer topics]
  (.subscribe consumer topics))

(defn consume
  ([consumer f]
   ;; TODO: fetch the default timeout value from global config
   (consume consumer f 2000))

  ([consumer f timeout]
   (try
     (while true
       (let [records (.poll consumer timeout)]
         (f records)))
     (finally
       (.close consumer)))))

(comment
  (def c (make-consumer {"bootstrap.servers" "localhost:9092"
                         "group.id"          "test"}))


  (subscribe c ["something"])
  (consume c (fn [x]
               (doseq [r x]
                 (println r))) 1000)
  (.close c))
