(ns hell-hound.connection.server
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [compojure.core :as compojure :refer [GET POST]]))


(def ring-ajax-post  (atom nil))
(def ring-handshake  (atom nil))

(def recv-ch
  "An atom containing channelSocket's receive channel"
  (atom nil))

(def send-fn!
  "An atom containing channelSocket's send API fn"
  (atom nil))


(def connected-uids
  "Watchable, read-only atom"
  nil)

(def event-router
  "An atom containing the current event router." nil)

(defn routes
  "Routes macro allows developer to setup sente connection and urls easy
  and painlessly."
  []
  (compojure/routes
   (compojure/context "/hellhound" []
                      (compojure/GET  "/" req (ring-handshake req))
                      (compojure/POST "/" req (ring-ajax-post req)))))



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

(defn -router [{:as ev-msg :keys [id ?data event]}]
  (-router ev-msg))


(defn initialize-event-router!
  "Initialize the sente connection along side with
  the event router. This function should be called
  at the initialization level of the application or
  start level of the component."
  []
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {:packer :edn})]

    (reset! ring-ajax-post  ajax-post-fn)
    (reset! ring-handshake  ajax-get-or-ws-handshake-fn)
    (reset! recv-ch         ch-recv)
    (reset! send-fn!        send-fn)
    (reset! connected-uids  connected-uids)
    (reset! event-router    (sente/start-server-chsk-router! ch-recv -router))

    ;; Returning a hashmap to be used in components.
    {:ring-ajax-post  ring-ajax-post
     :ring-handshake  ring-handshake
     :recv-ch         recv-ch
     :send-fn!        send-fn!
     :connected-uids  connected-uids
     :event-router    router}))
