(ns hellhound.core
  (:require [hellhound.connection :refer [send-fn!] ]))


(defn ->server
  [data]
  (let [send @send-fn!]
    (if (nil? send)
      (throw (js/Error. "Not connected to server."))
      (send data 5000))))

(defn dispatch->server
  "Dispatch the given event to server side application."
  [[event-name data]]
  (->server [:hellhound/event  {:event-name event-name :data data}]))
