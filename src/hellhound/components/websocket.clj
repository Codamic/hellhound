(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to."
  (:require [com.stuartsierra.component  :as component]
            [hellhound.connection.server :refer [initialize-event-router!]]))

(defrecord ^{:doc "Commmmm"}
    WebSocket []
  component/Lifecycle

  (start [component]
    (println "Establishing websocket server...")
    (let [details-map (initialize-event-router!)]
      (merge component details-map)))

  (stop [component]
    (println "Destroying websocket server...")
    (dissoc component [:ring-ajax-post :ring-handshake :recv-ch :send-fn!
                       :connected-uids :event-router])))


(defn make-websocket
  "Creates a websocket component."
  []
  (WebSocket.))
