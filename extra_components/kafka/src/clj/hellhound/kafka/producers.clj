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

(def default-key-serializer (StringSerializer.))
(def default-value-serializer (StringSerializer.))

(defn ^KafkaProducer make-producer
  ([config]
   (make-producer config default-key-serializer default-value-serializer))

  ([config key-serializer value-serializer]
   (let [producer-config (core/make-config config)]
     (KafkaProducer. producer-config key-serializer value-serializer))))
;

(defn send
  ([^Producer producer topic v]
   (let [record (ProducerRecord. topic v)]
     (.send producer record)))

  ([^Producer producer topic k v]
   (let [record (ProducerRecord. topic k v)]
     (.send producer record)))

  ([^Producer producer topic partition k v]
   (let [record (ProducerRecord. partition topic k v)]
     (.send producer record)))

  ([^Producer producer topic partition k v header]
   (let [record (ProducerRecord. partition topic k v header)]
     (.send producer record))))

(defn close
  [^Producer producer]
  (.close producer))

(comment
  (def p (make-producer {"bootstrap.servers" "localhost:9092"}))
  (send p "something" "11111111")
  (send p "something" "22222")
  (close p))
