(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to. In order to use this component all you
  have to do is, either use `websocket-server` function with
  `hellhound.system.defsystem` macro or use the `make-websocket` with a
  traditional system map."
  (:require [hellhound.connection.server             :refer [event-router]]
            ;[taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            (system.components [sente :refer [new-channel-sockets]])))


(defn make-websocket
  "Creates a websocket component instance."
  ([]
   (make-websocket {}))
  ([options]
   (new-channel-sockets event-router (get-sch-adapter) options)))


(defn websocket-server
  "Create an instance from websocket component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (websocket-server system-map {}))
  ([system-map options]
   (assoc-in system-map [:websocket] (make-websocket options))))
