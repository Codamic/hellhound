(ns hellhound.websocket
  "In order to connect to the HellHound's websocket server
  you need to use functions provided by this ns."
  [cljs.core.async.macros :as asyn]
  [hellhound.logger       :as log])

(defonce connection (atom nil))

(defn handle-connection-error
  []
  (log/error "Couldn't connect to remote server."))

(defn on-close
  []
  (log/info "Socket closed!"))

(defn on-recv
  [data]
  (async/put! input-channel data))

(defn set-channel!
  [connection packer]
  (let [input-channel  (create-input-channel)
        output-channel (create-output-channel)]
    (set! (.-onmessage connection)) (unpacked on-recv)
    (set! (.-onclose   connection)  on-close)))

(defn connect!
  "Connect to remote websocket server"
  [url {:keys [packer] :as options}]
  (if-let [conn (js/WebSocket url)]
    (set-channel! conn)
    (handle-connection-error)))
