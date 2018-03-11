(ns hellhound.kafka.core
  (:import
   [java.util Properties]
   [org.apache.kafka.streams KafkaStreams StreamsBuilder]
   [org.apache.kafka.streams.kstream KStream]))



(def _builder (atom nil))

(defn ^Properties make-config
  "Create a Properties instance using the given CONFIG hashmap."
  [config]
  (let [stream-config (Properties.)]
    (.putAll stream-config config)
    stream-config))

(defn ^StreamsBuilder builder
  []
  (when (nil? @_builder)
    (let [b (StreamsBuilder.)]
      (reset! _builder b)))
  @_builder)

(defn reset-builder!
  []
  (reset! _builder nil))

(defn ^KStream stream
  [topic]
  (.stream (builder) topic))

(defn ^KafkaStreams streams
  [config]
  (KafkaStreams. (.build (builder))
                 (make-config config)))


(reset! _builder nil)

(streams {"application.id"    "test.app"
          "bootstrap.servers" ["localhost:9092"]})
