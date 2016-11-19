(ns hellhound.connection.server
  (:require [hellhound.system :refer [get-system]]
            [compojure.core :as compojure :refer [GET POST]]))


(defn routes
  "Routes macro allows developer to setup sente connection and urls easy
  and painlessly."
  []
  (compojure/routes
   (compojure/context "/hellhound" []
                      (compojure/GET  "/" req ((:ring-ajax-get-or-ws-handshake (:websocket (get-system))) req))
                      (compojure/POST "/" req ((:ring-ajax-post (:websocket (get-system)))) req))))



(defmulti router
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defmethod router
   :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defmethod router :sam/sam
  [{:as ev-msg :keys [?data ?reply-fn event]}]
  (spit "/home/lxsameer/tmpdata" event :append true))

(defn event-router [{:as ev-msg :keys [id ?data event]}]
  (router ev-msg))
