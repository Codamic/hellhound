 (ns hellhound.kafka.producers
  "This namespace contains thin wrappers around the official
  Kafka Producer API. Here is a very simple example:

  ```clj
  (def p (make-producer {\"bootstrap.servers\" \"localhost:9092\"}))
  (send p \"sometopic\" \"something\")
  (send p \"sometopic\" \"something else\")
  (close p)
  ```
  For more information please consult the KafkaProducer javadocs."
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
  "Create a `KafkaProducer` instance by the given `config` map and optional
  `key-serializer` and `value-serializer` which are responsible for serializing
  key and value of records. For more information on them please checkout the
  javadocs for `org.apache.kafka.common.serialization` package."
  ([config]
   (make-producer config default-key-serializer default-value-serializer))

  ([config key-serializer value-serializer]
   (let [producer-config (core/make-config config)]
     (KafkaProducer. producer-config key-serializer value-serializer))))
;

(defn send
  "Send the given `key` (optional) and `value` to the given `topic` using
  the given `producer` and the optional `partition` and returns a java
  future object."
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
  "Close the given `producer` thread pool."
  [^Producer producer]
  (.close producer))
