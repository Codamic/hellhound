(ns hellhound.connection.client
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])

  (:require
   [re-frame.core   :as re-frame]
   [taoensso.encore :as encore :refer-macros (have have?)]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]))


(defonce handshake-ch (atom nil))
(defonce recv-ch      (atom nil))
(defonce send-fn!     (atom nil))
(defonce state        (atom nil))
(defonce router       (atom nil))


(defmulti dispatcher
  "A multimethod to dispatch and handle sente's events"
  :id)

(defn -router
  [{:as event-msg :keys [id event ?data]}]
  (dispatcher event-msg))

(defmethod dispatcher
  ; Default method is responsible for fallback and the situation
  ; which there is no handler matched
  :default
  [{:as event-msg :keys [event id]}]
  (js/console.warn (str "Missing handler for '" id "'. (Sente's event)")))

(defmethod dispatcher :chsk/recv
  [{:as event-msg :keys [event id ?data]}]
  (js/console.log (str "Re-frame event '" (first ?data) "' dispatched."))
  (re-frame/dispatch ?data))

(defmethod dispatcher :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (js/console.log (str "Handshake: " ?data))))

(defmethod dispatcher :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (js/console.log (str "Channel socket successfully established!: " new-state-map))
      (js/console.log (str "Channel socket state change: "              new-state-map)))))


(defn start-event-router!
  "This function should be called in the `handler.cljs` file to
  dispatch the server events into client application."
  []
  (let [{:as msg-map :keys [chsk ch-recv send-fn]}
        (sente/make-channel-socket! "/hellhound"
                                    {:type :auto
                                     :packer :edn})
        router_ (sente/start-client-chsk-router! ch-recv -router)]

    (reset! handshake-ch chsk)
    (reset! recv-ch      ch-recv)
    (reset! send-fn!     send-fn)
    (reset! state        (:state msg-map))
    (reset! router       router_)))
