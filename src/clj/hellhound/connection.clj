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
  [{:as ev-msg :keys [?data ?reply-fn event]}])


(defn event-router [{:as ev-msg :keys [id ?data event]}]
  (router ev-msg))



(defn send-to-all
  "Send the given event to all the connected users."
  [event]
  (let [func (:chsk-send! (:websocket (get-system)))]
    (func :sente/all-users-without-uid event)))
