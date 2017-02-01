(ns hellhound.components.websocket
  "Websocket component is responsible for setting up a sente server
  for client to connect to. In order to use this component all you
  have to do is, either use `websocket-server` function with
  `hellhound.system.defsystem` macro or use the `make-websocket` with a
  traditional system map."
  (:require [hellhound.connection.server             :refer [event-router]]
            [hellhound.system                        :refer [get-system]]
            [com.stuartsierra.component              :as component]
            [taoensso.sente                          :as sente]
            [taoensso.sente.interfaces               :as interfaces]
            [taoensso.encore :as enc :refer (swap-in! reset-in! swapped have have! have?)]
            [taoensso.sente.interfaces :as interfaces]
            ;[taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]))



(defn send-to-all
  [event]
  (let [func (:chsk-send! (:websocket (get-system)))]
    (func :sente/all-users-without-uid event)))

(defn- super-chain [^Class c]
  (when c
    (cons c (super-chain (.getSuperclass c)))))

(defn- pref
  ([] nil)
  ([a] a)
  ([^Class a ^Class b]
   (if (.isAssignableFrom a b) b a)))

(defn ff [protocol x]
  (println (str "T1: " (instance? (:on-interface protocol) x)))

  (if (instance? (:on-interface protocol) x)
    x
    (let [c (class x)
          impl #(get (:impls protocol) %)]
      (println (str "T2: " c))
      (println (str "T3: " (impl c)))
      (println (str "T4: " (first (remove nil? (map impl (butlast (super-chain c)))))))
      (println (str "T5: " (when-let [t (reduce pref (filter impl (disj (supers c) Object)))]
                             (impl t))))
      (println (str "T6: " (impl Object)))

      (or (impl c)
          (and c (or (first (remove nil? (map impl (butlast (super-chain c)))))
                     (when-let [t (reduce pref (filter impl (disj (supers c) Object)))]
                       (impl t))
                     (impl Object)))))))
(defn s?
  "Returns true if x satisfies the protocol"
  [protocol x]
  (boolean (ff protocol x)))


(defrecord WebSocketServer [web-server-adapter handler options adapter]
  component/Lifecycle
  (start [component]
    (println "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    (println (s? interfaces/IServerChanAdapter adapter))
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
      (if (not (nil? router))
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
   (map->WebSocketServer {:web-server-adapter web-server-adapter
                          :handler event-msg-handler
                          :options options
                          :adapter (get-sch-adapter)})))


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
