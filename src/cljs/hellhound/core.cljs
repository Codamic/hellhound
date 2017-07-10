(ns hellhound.core
  (:require
   [reagent.core         :as reagent]
   [re-frame.core         :as re-frame]
   [re-frisk.core        :as re-frisk]
   [hellhound.connection :as connection]))


(def debug?
  ^boolean js/goog.DEBUG)

(defn ->server
  "Send the given `data` to the server."
  [data]
  (let [send @connection/send-fn!]
    (if (nil? send)
      (throw (js/Error. "Not connected to server."))
      (send data 5000))))

(defn dispatch->server
  "Dispatch the given event to server side application."
  [[name data]]
  (->server [:hellhound/message {:message-name name :data data}]))

(defn mount-root
  [view]
  (re-frame/clear-subscription-cache!)
  (reagent/render [view]
                  (.getElementById js/document "app")))

(defn setup-development
  [dev-fn]
  (enable-console-print!)
  (re-frisk/enable-re-frisk!)
  (println "DEV MODE")
  (dev-fn))

(defn init!
  "Initialize the client side application."
  [{:keys [router dev-setup main-view dispatch-events] :as options}]

  (re-frame/dispatch-sync dispatch-events)
  (when debug?
    (setup-development dev-setup))
  (mount-root main-view)
  (connection/start-event-router! "localhost:3000"))
