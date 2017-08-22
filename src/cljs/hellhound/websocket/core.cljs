(ns hellhound.websocket.core
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :as async :refer [<! >! put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; TODO: We need to make buffer size of channels configurable
(def default-ws-config
  {:read-buffer 1000
   :write-buffer 1000
   :data-format :json})

(defn connect-to
  [address {:keys [read-buffer write-buffer data-format]}]
  (js/console.log "XXX" data-format)
  (ws-ch address
         {:read-ch (async/chan read-buffer)
          :write-ch (async/chan write-buffer)
          :format data-format}))

(defn connect
  ([address]
   (connect address {}))
  ([address options]
   (let [config (merge default-ws-config options)]
     (go
       (let [{:keys [ws-channel connection-error]} (<! (connect-to address config))]
         (if connection-error
           (js/console.log "Can't connect to the server.")
           (>! ws-channel {:a 1})
           (js/console.log "Got message from server" (pr-str (<! ws-channel)))))))))
