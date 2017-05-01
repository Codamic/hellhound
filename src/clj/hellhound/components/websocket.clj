(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to. In order to use this component all you
  have to do is, either use `websocket-server` function with
  `hellhound.system.defsystem` macro or use the `make-websocket` with a
  traditional system map."
  (:require
   [hellhound.connection                    :refer [router-builder]]
   [hellhound.system                        :refer [get-system]]
   [hellhound.components.core               :as component]
   [taoensso.sente.packers.transit          :as packer]
   [taoensso.sente                          :as sente]
   [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]))

;; This component is responsible for establish a websocket server
;; Base on Sente
(defrecord WebSocketServer [web-server-adapter handler options adapter]
  component/Lifecycle
  (start [component]
    (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
          (sente/make-channel-socket-server! adapter  options) ;;web-server-adapter
          component (assoc component
                           :ring-ajax-post                ajax-post-fn
                           :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
                           :ch-chsk                       ch-recv
                           :chsk-send!                    send-fn
                           :adapter                       adapter
                           :connected-uids                connected-uids)]

      (assoc component
             :router (sente/start-chsk-router!
                      ch-recv (if (:wrap-component? options)
                                (handler component)
                                handler)))))
  (stop [component]
    (let [router (:router component)]
      (if-not (nil? router)
        (do
          (router)
          (assoc component :router nil))
        component))))



(defn new-channel-socket-server
  ([web-server-adapter]
   (new-channel-socket-server nil web-server-adapter {}))
  ([event-msg-handler web-server-adapter]
   (new-channel-socket-server event-msg-handler web-server-adapter {}))
  ([event-msg-handler web-server-adapter options]
   (let [opts (merge {:packer  (packer/->TransitPacker :json {} {})}
                     options)]
     (map->WebSocketServer {:web-server-adapter web-server-adapter
                            :handler event-msg-handler
                            :options opts
                            :adapter (get-sch-adapter)}))))


(defn make-websocket
  "Creates a websocket component instance."
  ([router]
   (new-channel-socket-server (router-builder router) (get-sch-adapter) {}))
  ([router options]
   (new-channel-socket-server (router-builder router) (get-sch-adapter) options)))


(defn websocket-server
  "Create an instance from websocket component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (websocket-server system-map {}))
  ([system-map options]
   (update-in system-map [:components :websocket] (make-websocket options))))
