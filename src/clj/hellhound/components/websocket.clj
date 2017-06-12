(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to. In order to use this component all you
  have to do is, either use `websocket-server` function with
  `hellhound.system.defsystem` macro or use the `make-websocket` with a
  traditional system map."
  (:require
   [hellhound.connection                    :refer [router-builder]]
   [hellhound.components.protocols               :as protocols]
   [taoensso.sente.packers.transit          :as packer]
   [taoensso.sente                          :as sente]
   [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]))

;; This component is responsible for establish a websocket server
;; Base on Sente
(defrecord WebSocketServer [web-server-adapter handler options adapter]
  protocols/Lifecycle
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
   ;; TODO: Refactor this `let` statement
   (let [opts (merge {:packer  (packer/->TransitPacker :json {} {})
                      ;; TODO: Use something elegant instead of the epoch time for uids
                      :user-id-fn  (fn [_] (str (quot (System/currentTimeMillis) 1000)))}
                     options)]
     (map->WebSocketServer {:web-server-adapter web-server-adapter
                            :handler event-msg-handler
                            :options opts
                            :adapter (get-sch-adapter)}))))


(defn new-websocket
  "Creates a websocket component instance."
  ([router]
   (new-channel-socket-server (router-builder router) (get-sch-adapter) {}))
  ([router options]
   (new-channel-socket-server (router-builder router) (get-sch-adapter) options)))


(defn make-websocket-component
  "Create an instance from websocket component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (make-websocket-component system-map {}))
  ([system-map options]
   (update-in system-map [:components :websocket] (new-websocket options))))
