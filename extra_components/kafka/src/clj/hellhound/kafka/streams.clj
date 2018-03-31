(ns hellhound.kafka.streams
  (:require [manifold.stream :as stream])
  (:import
   [java.util Properties]
   [org.apache.kafka.streams KafkaStreams StreamsBuilder StreamsConfig]
   [org.apache.kafka.streams.kstream KStream]))

(def _builder (atom nil))


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
