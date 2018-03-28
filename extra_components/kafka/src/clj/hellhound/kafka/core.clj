(ns hellhound.kafka.core
  (:require [manifold.stream :as stream])
  (:import
   [java.util Properties]
   [org.apache.kafka.streams KafkaStreams StreamsBuilder StreamsConfig]
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


(def cb (reify org.apache.kafka.streams.kstream.ForeachAction
          (apply [this k v]
            (println (str "<<<<<<<<<<<<<< " k ">>>> " v)))))

(defn xx []
  (reset! _builder nil)
  (let [ss (stream "something")
        a (streams {"application.id"    "test.app"
                    "retries"           "1"
                    (StreamsConfig/BOOTSTRAP_SERVERS_CONFIG) "localhost:9092"})]
                    ;;"bootstrap.servers" "localhost:9092"})]

    (.foreach ss cb)
    (.start a)
    a))

(defn cc [a]
  (.close a))


(def x (xx))
(cc x)
