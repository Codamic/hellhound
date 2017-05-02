(ns hellhound.core
  (:require [hellhound.connection :refer [send-fn!] ]))


(defn ->server
  "Send the given `data` to the server."
  [data]
  (let [send @send-fn!]
    (if (nil? send)
      (throw (js/Error. "Not connected to server."))
      (send data 5000))))

(defn dispatch->server
  "Dispatch the given event to server side application."
  [[name data]]
  (->server [:hellhound/message {:message-name name :data data}]))
