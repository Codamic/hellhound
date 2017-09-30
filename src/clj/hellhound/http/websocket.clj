(ns hellhound.http.websocket
  "Websocket module of HellHound is responsible for creating
  a websocket server on top of Immutant server and dispatch
  the event coming from clients to `event-router` and send
  back the result"
  (:require
   [clojure.core.async                   :as async]
   [clj-uuid                             :as uuid]
   [hellhound.logger                     :as log]
   [hellhound.http.websocket.json        :as json]))

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
  [send-ch]
  (log/info "CH: " (class send-ch))
  (let [id 2]
    (async/put! send-ch id)
    (swap! clients assoc id {:send-fn send-ch})))

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

;;(def ws-on-connect (websocket/start-ws-connection new-client-connected!))

(defn ws-on-text
  "Default `on-text` callback for the websocket server"
  [session msg]
  (log/info "<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
  (log/info (class session))
  (log/info :msg (str "A client sent " msg)))

(defn ws-on-binary
  "Default `on-text` callback for the websocket server"
  [payload offset length]
  (log/info :msg "Binary Message!" :bytes payload))

(defn ws-on-error
  [session t]
  (log/error :msg "WS Error happened" :exception t))

(defn ws-on-close
  [channel {:keys [code reason]}]
  (log/info :msg "WS Closed:" :reason reason))

;; (defn ws-routes
;;   [req url packer {:keys [on-connect on-text on-binary on-error on-close]
;;                    :as   options
;;                    :or   {on-connect ws-on-connect
;;                           on-text    ws-on-text
;;                           on-binary  ws-on-binary
;;                           on-error   ws-on-error
;;                           on-close   ws-on-close}}]

;;   (clojure.pprint/pprint req)
;;   {url
;;    {:on-connect ws-on-connect
;;     :on-message ws-on-text
;;     ;;:on-binary  ws-on-binary
;;     :on-error   ws-on-error
;;     :on-close   ws-on-close}})

;; (defn add-endpoint
;;   [{:keys [url packer]
;;     :as   options
;;     :or   {url    "/hellhound/ws"
;;            packer (json/->JsonPacker)}}]
;;   (fn [request]
;;     (websocket/add-ws-endpoints request (ws-routes request url packer options))))

;; (defn add-websocket
;;   "Add websocket endpoints to the given `service-map`."
;;   [service-map {:keys [packer url] :as options}])
