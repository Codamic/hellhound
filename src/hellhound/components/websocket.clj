(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to."
  (:require [hellhound.connection.server             :refer [event-router]]
            ;[taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            (system.components [sente :refer [new-channel-sockets]])))


(defn make-websocket
  "Creates a websocket component."
  ([]
   (make-websocket {}))
  ([options]
   (new-channel-sockets event-router (get-sch-adapter) options)))


(defn websocket-server
  ([system-map]
   (websocket-server system-map {}))
  ([system-map options]
   (assoc-in system-map [:websocket] (make-websocket options))))
