(ns hellhound.frontend.core
  (:require [hellhound.connection.client :refer [send-fn!] ]))


(defn- send-to-server
  [data]
  (let [send @send-fn!]
    (if (nil? send)
      (println "Not connected to server.")
      (do
        (println "Sent to server")
        (println data)
        (send data 5000)))))

(defn dispatch->server
  "Dispatch the given event to server side application."
  [event]
  (send-to-server event))
