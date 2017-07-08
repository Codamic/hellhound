(ns hellhound.connection
  "This namespace is responsible for dispatching reveived data
  from the client to the correct message handler in the server side
  application. In order to define a message router checkout the
  `hellhound.messaging.core` namespace. This namespace is an internal
  namespace which used by the `websocket` component."
  (:require [hellhound.components      :as components]
            [hellhound.logger.core     :as logger]))



(defmulti router
  "Multimethod to handle Sente `event-msg`s. Dispatch messages by `:id`"
  :id)

(defmethod router
   :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (logger/warn (str "Unhandled event: " event))
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defmethod router :chsk/ws-ping
  [{:as ev-msg :keys [?data ?reply-fn event]}]
  (logger/warn "TODO: Handle ws-ping event"))


(defmethod router :hellhound/message
  [{:as ev-msg :keys [?data ?reply-fn event message-router uid
                      client-id send-fn]}]
  (logger/warn "TODO: Handle hellhound event")

  (let [message-name    (:message-name ?data)
        message-handler (get message-router message-name)]
    (if (nil? message-handler)
      ;; TODO: Should we send any error code or something similar?
      (logger/warn "Can't find an event handler for '%s' event" message-name)

      ;; Calls the message handler function provided by the message router
      ;; with the following arguments:
      ;; First argument is the `data` which sent by the client, second argument
      ;; is a `function` to send data back to the same client and finally the
      ;; third argument is the `ev-msg` itself which contains complete
      ;; on the received message information.
      (message-handler (:data ?data) #(send-fn uid %) ev-msg))))

(defn event-router [{:as ev-msg :keys [id ?data event]}]
  (router ev-msg))

(defn router-builder
  [router-map]
  (fn [{:as ev-msg :keys [id ?data event]}]
    (let [event-map (assoc ev-msg :message-router router-map)]
      (router event-map))))

(defn send-to-all
  "Send the given event to all the connected users."
  [event]
  (let [func (:chsk-send! (components/get-component :websocket))]
    (func :sente/all-users-without-uid event)))
