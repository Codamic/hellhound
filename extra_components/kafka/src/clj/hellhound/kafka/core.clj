(ns hellhound.kafka.core
  (:import
   [java.util Properties]
   [org.apache.kafka.streams KafkaStreams]
   ;;[org.apache.kafka.streams StreamsConfig]
   [org.apache.kafka.streams.kstream KStreamsBuilder Kstream]
   [org.apache.kafka.streams.processor Topology]))

(def _builder (atom nil))

(defn ^Properties make-config
  "Create a Properties instance using the given CONFIG hashmap."
  [config]
  (let [stream-config (Properties.)]
    (.putAll stream-config config)
    stream-config))

(defn ^KStreamBuilder builder
  []
  (if (nil? @_builder)
    (let [b (KStreamBuilder.)]
      (reset! _builder b))
    @_builder))

(defn ^KStream stream
  [topic]
  (.stream (builder) topic))

(defn ^KafkaStreams streams
  []
  (KafkaStreams. (builder) (make-config config)))
