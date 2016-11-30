(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to. In order to use this component all you
  have to do is, either use `websocket-server` function with
  `hellhound.system.defsystem` macro or use the `make-websocket` with a
  traditional system map."
  (:require [hellhound.connection.server             :refer [event-router]]
            [com.stuartsierra.component              :as component]
            [taoensso.sente :as sente]
            ;[taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))





(defrecord WebSocketServer [ring-ajax-post ring-ajax-get-or-ws-handshake ch-chsk chsk-send! connected-uids router web-server-adapter handler options]
  component/Lifecycle
  (start [component]
    (let [handler (get-in component [:sente-handler :handler] handler)
          {:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
          (sente/make-channel-socket-server! web-server-adapter options)
          component (assoc component
                           :ring-ajax-post ajax-post-fn
                           :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
                           :ch-chsk ch-recv
                           :chsk-send! send-fn
                           :connected-uids connected-uids)]
      (assoc component
             :router (sente/start-chsk-router!
                      ch-recv (if (:wrap-component? options)
                                (handler component)
                                handler)))))
  (stop [component]
    (if-let [stop-f router]
      (assoc component :router (stop-f))
      component)))



(defn new-channel-socket-server
  ([web-server-adapter]
   (new-channel-socket-server nil web-server-adapter {}))
  ([event-msg-handler web-server-adapter]
   (new-channel-socket-server event-msg-handler web-server-adapter {}))
  ([event-msg-handler web-server-adapter options]
   (map->WebSocketServer {:web-server-adapter web-server-adapter
                              :handler event-msg-handler
                              :options options})))


(defn make-websocket
  "Creates a websocket component instance."
  ([]
   (make-websocket {}))
  ([options]
   (new-channel-socket-server event-router (get-sch-adapter) options)))


(defn websocket-server
  "Create an instance from websocket component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (websocket-server system-map {}))
  ([system-map options]
   (assoc-in system-map [:websocket] (make-websocket options))))
