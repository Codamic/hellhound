(ns hellhound.http.websocket
  "Websocket module of HellHound is responsible for creating
  a websocket server on top of Immutant server and dispatch
  the event coming from clients to `event-router` and send
  back the result"
  (:require
   [clojure.core.async            :as async]
   [aleph.http                    :as http]
   [clj-uuid                      :as uuid]
   [manifold.stream               :as stream]
   [manifold.deferred             :as deferred]
   [hellhound.logger              :as log]
   [hellhound.http.websocket.json :as json]
   [manifold.deferred :as d]))

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

(defn bad-request
  ([context]
   (bad-request context ""))
  ([{:keys [request]} msg]
   {:status 400
    :headers {"content-type" "application/text"}
    :body (str "Expected a websocket request." msg)}))


;; (defn setup-event-router
;;   [output router]
;;   (fn [msg]
;;     (let [handler (:hello router)]
;;       (stream/put! output (handler {:msg (jpack/unpack msg)})))))

(defn upgraded
  [context]
  {:status 101})

(defn accept-ws
  [{:keys [request input output uid send->user?] :as context}]
  (->
   (deferred/chain
     (http/websocket-connection request))
   (deferred/catch
       Exception
       ;; TODO: We need to return the exception message
       ;; instead of its instance.
       (fn [e] (bad-request context (.getMessage e))))))

(defn ws
  [{:keys [uid] :as context}]
  (if-not uid
    (assoc context
           :response
           (bad-request context))

    (assoc context
           :response      (upgraded context)
           :ws-stream     @(accept-ws context))))


(defn get-or-create-user-id
  [context]
  ;; TODO: check the cookies for the presence of uid
  ;; otherwise create a new one.
  (assoc context
         :uid (str (java.util.UUID/randomUUID))))

(defn setup-ws-output
  [{:keys [uid output socket] :as context}]
  (when uid
    ;; TODO: inject the uid or even the context to the
    ;; outgoing message.
    (stream/connect socket output))
  ;; TODO: If uid was nil, should we kill the connection ?
  context)

(defn setup-ws-input
  [{:keys [uid input socket send->user?] :as context}]
  (when uid
    (stream/connect-via input
                        #(when (send->user? context %) (stream/put! socket %))
                        socket))
  context)

(defn interceptor-factory
  []
  [{:name  ::ws-user-id-interceptor
    :enter get-or-create-user-id}

   {:name  ::ws-connection-interceptor
    :enter ws}

   {:name  ::ws-output-interceptor
    :enter setup-ws-output}

   {:name  ::ws-input-interceptor
    :enter setup-ws-input}])
