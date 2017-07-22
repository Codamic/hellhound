(ns hellhound.http.websocket
  "Websocket module of HellHound is responsible for creating
  a websocket server on top of Immutant server and dispatch
  the event coming from clients to `event-router` and send
  back the result"
  (:require
   [clojure.core.async                   :as async]
   [clj-uuid                             :as uuid]
   [io.pedestal.http.jetty.websockets    :as websocket]
   [hellhound.logger                     :as log]))

;; An atom containing all the connected clients
(def clients (atom {}))


(defn get-client-id
  "Returns an id for the given socket session"
  [ws-session]
  (let [address (.getRemoteAddress ws-session)
        uuid    (uuid/v5 uuid/+namespace-url+ address)]
    (log/info "USER: " uuid)
    (str uuid)))


(defn new-client-connected!
  "This function is responsible to populate the client attom
   when a new client joined"
  [ws-session send-ch]
  (let [id (get-client-id ws-session)]
    (async/put! send-ch id)
    (swap! clients assoc id {:session ws-session
                             :send-fn send-ch})))

;; This is just for demo purposes
(defn send-and-close! []
  (let [[ws-session send-ch] (first @clients)]
    (async/put! send-ch "A message from the server")
    ;; And now let's close it down...
    (async/close! send-ch)
    ;; And now clean up
    (swap! clients dissoc ws-session)))

;; Also for demo purposes...
(defn send-message-to-all!
  [message]
  (doseq [[session channel] @clients]
    ;; The Pedestal Websocket API performs all defensive checks before sending,
    ;;  like `.isOpen`, but this example shows you can make calls directly on
    ;;  on the Session object if you need to
    (when (.isOpen session))))
`      (async/put! channel message)

(def ws-routes
  {"/hellhound/ws"
   {
    :on-connect (websocket/start-ws-connection new-client-connected!)
    :on-text    (fn [msg] (log/info :msg (str "A client sent - " msg)))
    :on-binary  (fn [payload offset length] (log/info :msg "Binary Message!" :bytes payload))
    :on-error   (fn [t] (log/error :msg "WS Error happened" :exception t))
    :on-close   (fn [num-code reason-text]
                  (log/info :msg "WS Closed:" :reason reason-text))}})

(defn add-endpoint
  [request]
  (websocket/add-ws-endpoints request ws-routes))
