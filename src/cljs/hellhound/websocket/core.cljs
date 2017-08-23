(ns hellhound.websocket.core
  (:require [chord.client  :as chord]
            [re-frame.core :as re-freme]
            [cljs.core.async :as async :refer [<! >! put! close!]]
            [hellhound.logger :as log])

  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ws-connection (atom))

;; TODO: We need to make buffer size of channels configurable
(def default-ws-config
  {:read-buffer 1000
   :write-buffer 1000
   :data-format :json})

(defn send
  [data]
  (let [socket @ws-connection]
    (if socket
      (go (>! socket data))
      (throw (Error. "WebSocket connection is not present.")))))

(defn connect-to
  [address {:keys [read-buffer write-buffer data-format]}]
  (chord/ws-ch address
         {:read-ch (async/chan read-buffer)
          :write-ch (async/chan write-buffer)
          :format data-format}))

(defn set-global-connection!
  [channel]
  (log/debug "Resetting global ws connection...")
  (reset! ws-connection channel))

(defn handle-connecton
  [ws-channel]
  (set-global-connection! ws-channel)
  (let [{:keys [message error]} (<! ws-channel)]
    (if error
      (do
        (log/error "Error in receiving message. Error:")
        (log/error error)
        (re-frame/dispatch [:message-error error]))
      (do
        (log/debug "Message Received. Message:")
        (log/debug message)
        (re-frame/dispatch [:message-received message])))))

(defn handle-connection-error
  [error]
  (log/error "Can't connect to the server due to:")
  (log/error error)
  ;; TODO: Do we need fully qualified keywords here?
  (re-frame/dispatch [:connection-error error]))

(defn connect
  ([address]
   (connect address {}))
  ([address options]
   (let [config (merge default-ws-config options)]
     (go
       (let [{:keys [ws-channel connection-error]} (<! (connect-to address config))]
         (if connection-error
           (handle-connection-error connection-error)
           (handle-connection ws-channel)))))))
