(ns hellhound.http.websocket
  "Websocket module of HellHound is responsible for creating
  a websocket server on top of Immutant server and dispatch
  the event coming from clients to `event-router` and send
  back the result"
  (:require
   [clojure.core.async                   :as async]
   [io.pedestal.http.immutant.websockets :as websocket]
   [hellhound.logger                     :as log]))


(def clients (atom {}))

(defn new-client
  [ws-session send-ch]
  ;; TODO: Send connection info to client
  (async/put! send-ch "This will be a text message")
  ;; TODO assoc a strcuture instead of a function
  (swap! clients assoc ws-session send-ch))

;; This is just for demo purposes
(defn send-and-close! []
  (let [[ws-session send-ch] (first @ws-clients)]
    (async/put! send-ch "A message from the server")
    ;; And now let's close it down...
    (async/close! send-ch)
    ;; And now clean up
    (swap! clients dissoc ws-session)))

;; Also for demo purposes...
(defn send-message-to-all!
  [message]
  (doseq [[^Session session channel] @ws-clients]
    ;; The Pedestal Websocket API performs all defensive checks before sending,
    ;;  like `.isOpen`, but this example shows you can make calls directly on
    ;;  on the Session object if you need to
    (when (.isOpen session)
      (async/put! channel message))))

(def ws-routes
  {"/hellhound/ws"
   {
    :on-connect (webscoket/start-ws-connection new-client)
    :on-text    (fn [msg] (log/info :msg (str "A client sent - " msg)))
    :on-binary  (fn [payload offset length] (log/info :msg "Binary Message!" :bytes payload))
    :on-error   (fn [t] (log/error :msg "WS Error happened" :exception t))
    :on-close   (fn [num-code reason-text])
                (log/info :msg "WS Closed:" :reason reason-text)}})



(defn add-endpoint
  [request]
  (websocket/add-ws-endpoint request ws-routes))
