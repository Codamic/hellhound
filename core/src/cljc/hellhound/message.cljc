(ns hellhound.message
  (:require
   [hellhound.message.protocols :as impl]
   [hellhound.message.impl.message]))

(def id impl/id)
(def resolvers impl/resolvers)
(def enqueue-resolver impl/enqueue-resolver)
(def resolve! impl/resolve!)


(defn create
  ([]
   (create {}))
  ([initial-value]
   (impl/init initial-value)))

(comment
  (impl/resolvers (enqueue-resolver (create) #(println %)))

  (let [m (create)]
    (impl/resolve! (impl/enqueue-resolver m #(println %)))))
