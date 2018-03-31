(ns hellhound.kafka.consumers
  (:require
   [hellhound.kafka.core :as core])
  (:import
   [org.apache.kafka.clients.consumer
    KafkaConsumer
    Consumer]
   [org.apache.kafka.common.serialization StringDeserializer]))


(def default-key-serializer (StringDeserializer.))
(def default-value-deserializer (StringDeserializer.))

(defn ^KafkaConsumer make-consumer
  "Create a kafka consumer using the given `config` and key value serializers.

  `key-deserializer` and `value-deserializer` are going to deserialize the
  respected value from kafka to clojure values. The default deserializer
  for both of them is `StringDeserializer`."
  ([config]
   (make-consumer config
                  default-key-serializer
                  default-value-deserializer))

  ([config key-deserializer value-deserializer]
   (let [props (core/make-config config)]
     (KafkaConsumer. props key-deserializer value-deserializer))))

(defn subscribe
  "Subscribe to the given list of `topics` of the given `consumer`."
  [^Consumer consumer topics]
  (.subscribe consumer topics))

(defn consume
  "Consume records from the given `consumer` and apply function`f` on them.

  By default consumed values are a `ConsumerRecords` java object.
  An optional third arg is the `timeout` for the connection. The default value
  for the timeout is `2000ms`."
  ([^Consumer consumer f]
   ;; TODO: fetch the default timeout value from global config
   (consume consumer f 2000))

  ([^Consumer consumer f timeout]
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
