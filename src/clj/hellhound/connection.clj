(ns hellhound.connection
  (:require [hellhound.system :refer [get-system]]
            [hellhound.logger.core :as logger]))



(defmulti router
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

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


(defmethod router :hellhound/event
  [{:as ev-msg :keys [?data ?reply-fn event event-router send-fn]}]
  (logger/warn "TODO: Handle hellhound event")

  (let [event-name    (:event-name ?data)
        event-handler (get event-router event-name)]
    (if (nil? event-handler)
      ;; TODO: Should we send any error code or something similar?
      (logger/warn "Can't find an event handler for '%s' event" event-name)
      (event-handler (:data ?data) ev-msg))))

(defn event-router [{:as ev-msg :keys [id ?data event]}]
  (router ev-msg))

(defn router-builder
  [router-map]
  (fn [{:as ev-msg :keys [id ?data event]}]
    (let [event-map (assoc ev-msg :event-router router-map)]
      (router event-map))))

(defn send-to-all
  "Send the given event to all the connected users."
  [event]
  (let [func (:chsk-send! (:websocket (get-system)))]
    (func :sente/all-users-without-uid event)))
