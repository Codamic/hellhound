(ns hellhound.kafka.producers
  (:require
   [hellhound.kafka.core :as core])

  (:import
   [org.apache.kafka.clients.producer
    KafkaProducer
    ProducerRecord
    Producer]

   [org.apache.kafka.common.serialization
    StringSerializer]))

(def default-key-serializer StringSerializer)
(def default-value-serializer StringSerializer)

(defn ^KafkaProducer make-producer
  ([config]
   (make-producer config default-key-serializer default-value-deserializer))

  ([config key-serializer value-serializer]
   (let [producer-config (core/make-config config)]
     (KafkaProducer. config key-serializer value-serializer))))
;

(defn send
  [^Producer producer topic k v]
  (let [record (ProducerRecord. topic k v)]
    (.send producer record)))


(comment
  (let [p (make-producer {"bootstrap.servers" "localhost:9092"})]
    (send p "something")))
