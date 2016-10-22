(ns hell-hound.connection.client
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])

  (:require
   [re-frame.core   :as re-frame]
   [taoensso.encore :as encore :refer-macros (have have?)]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]))


(defn -router
  [{:as event-msg :keys [id event ?data]}]
  (router event-msg))


(defmulti router
  "A multimethod to dispatch and handle sente's events"
  :id)

(defmethod router
  ; Default method is responsible for fallback and the situation
  ; which there is no handler matched
  :default
  [{:as event-msg :keys [event id]}]
  (js/console.warn (str "Missing handler for '" id "'. (Sente's event)")))

(defmethod router :chsk/recv
  [{:as event-msg :keys [event id ?data]}]
  (js/console.log (str "Re-frame event '" (first ?data) "' dispatched."))
  (re-frame/dispatch ?data))

(defmethod router :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (js/console.log (str "Handshake: " ?data))))

(defmethod router :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (js/console.log (str "Channel socket successfully established!: " new-state-map))
      (js/console.log (str "Channel socket state change: "              new-state-map)))))


(defn start-event-router!
  "This function should be called in the `handler.cljs` file to
  dispatch the server events into client application."
  []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket! "/hellhound"
                                    {:type :auto
                                     :packer :edn})]
    {:chsk       chsk
     :ch-chsk    ch-recv ; ChannelSocket's receive channel
     :chsk-send! send-fn ; ChannelSocket's send API fn
     :chsk-state state   ; Watchable, read-only atom
     :router     (sente/start-client-chsk-router! ch-recv -router)}))
