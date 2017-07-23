(ns hellhound.websocket
  "In order to connect to the HellHound's websocket server
  you need to use functions provided by this ns."
  (:require
   [cljs.core.async        :as async]
   [hellhound.logger       :as log]))

(defonce connection (atom nil))

(defprotocol IPacker
  (pack   [this data])
  (unpack [this data]))

(deftype JsonPacker []
  IPacker
  (pack   [this data] (js/JSON.stringify data))
  (unpack [this data] (js/JSON.parse     data)))

(defn handle-connection-error
  []
  (log/error "Couldn't connect to remote server."))

(defn on-close
  []
  (log/info "Socket closed!"))

(defn on-recv
  [packet packer chan]
  (if-not (satisfies? IPacker packer)
    (throw "The provided packer does not satisfies IPacker protocol."))

  (let [data (unpack packer packet)]
    (log/debug "DATA RECV:")
    (log/debug data)
    (async/put! chan data)))

(defn create-recv-channel
  []
  (async/chan 100))

(defn create-send-channel
  []
  (async/chan 100))

(defn set-channels!
  [connection packer]

  (let [recv-channel (create-recv-channel)
        send-channel (create-send-channel)]

    (set! (.-onmessage connection) #(on-recv % packer recv-channel))
    (set! (.-onclose   connection) on-close)

    {:connection   connection
     packer        packer
     :recv-channel recv-channel
     :send-channel send-channel}))





(defn create-json-packer
  []
  (JsonPacker.))

(defn connect!
  "Connect to remote websocket server"
  ([url]
   (connect! url {}))

  ([url {:keys [packer]
         :as options
         :or {packer  (create-json-packer)}}]

   (if-let [conn (js/WebSocket. url)]
     (set-channels! conn packer)
     (handle-connection-error))))
