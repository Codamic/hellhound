(ns hell-hound.components.websocket
  (:require [com.stuartsierra.component :as component]
            [hell-hound.connection.server :refer [initialize-event-router!]]))

(defrecord WebSocket []
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
