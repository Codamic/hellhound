(ns hellhound.components.kafka-consumer
  (:require
   [hellhound.kafka.consumers :as consumers]
   [hellhound.core :as hellhound]
   [hellhound.component :as hcomp]
   [manifold.stream :as s]
   [manifold.deferred :as d]))


(defn make-config
  [config]
  (merge (hellhound/get-config :kafka :consumers)
         config))

(defn start!
  [config topics]
  (fn [this context]
    (let [[input output] (hcomp/io this)
          c              (consumers/make-consumer config)
          active?   (atom true)]

      (consumers/subscribe c topics)
      (assoc this
             :consumer c
             :active? active?
             :poll-loop (d/future (consumers/consume-each
                                   #(deref active?)
                                   #(s/put! output %)))))))


(defn stop!
  [this]
  (when (:consumer this)
    (reset! (:active? this) false)
    (consumers/close (:consumer this)))

  (dissoc this :consumer))

(defn factory
  [config topic]
  (hcomp/make-component :hellhound.components/kafka-consumer
                        (start! config topic)
                        stop!))
