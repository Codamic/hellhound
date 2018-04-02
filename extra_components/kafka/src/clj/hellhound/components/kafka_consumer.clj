(ns hellhound.components.kafka-consumer
  (:require
   [hellhound.kafka.consumers :as consumer]
   [hellhound.core :as hellhound]
   [hellhound.component :as hcomp]
   [manifold.stream :as s]
   [manifold.deferred :as d]
   [kafka-clj.client :as kafka]))

(defn make-config
  [config]
  (merge (hellhound/get-config :kafka :consumers)
         config))

(defn start!
  [config topics]
  (fn [this context]
    (let [[input output] (hcomp/io this)
          c              (consumer/make-consumer config)
          active-loop?   (atom true)]

      (consumer/subscribe topics)
      (assoc this
             :consumer c
             :poll-loop (d/future (consumer/consume-each
                                   #(deref active-loop?
                                   #(s/put! output %))))))))


(defn stop!
  [this]
  (when (:consumer this)
    (consumer/stop (:consumer this)))
  (dissoc this :consumer))

(defn factory
  [config topic]
  (hcomp/make-component ::hellhound.components/kafka-consumer
                        (start! config topic)
                        stop!))
