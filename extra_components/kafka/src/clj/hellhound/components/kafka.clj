(ns hellhound.components.kafka
  (:require [kafka-clj.client    :as kafka]
            [hellhound.core      :as hellhound]
            [hellhound.component :as component]))


(defn start
  [address config]

  (fn
    [this context]
    (let [[input output] (component/io this)
          connector      (kafka/create-connector address config)]
      (stream/consume #(kafka/send-msg connector %) input)
      (assoc this :connector connector))))

(defn stop
  [this])








(or (hellhound/get-config [:kafka :host]) "localhost")
(or (hellhound/get-config [:kafka :port]) "9092")
