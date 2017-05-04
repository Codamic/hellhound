(ns hellhound.messaging.helpers
  "Several helpers function to make the communication with client side
  easier"
  (:require [hellhound.components.core :refer [get-component]]))

(def APP-DB-UPDATE-EVENT :app-db/update)
(def APP-DB-APPEND-EVENT :app-db/append)

(defn update-app-db
  "Update the client side `app-db` of the client with the given uid
  with given keys vectore with the given `value`"
  [uid keys-vec value]
  (let [send-fn (:chsk-send! (get-component :websocket))]
    (send-fn uid [APP-DB-UPDATE-EVENT {:keys keys-vec
                                       :value value}])))

(defn append->app-db
  "Update the client side `app-db` of the client with the given uid
  with given keys vectore with the given `value`"
  [uid keys-vec value]
  (let [send-fn (:chsk-send! (get-component :websocket))]
    (send-fn uid [APP-DB-APPEND-EVENT {:keys keys-vec
                                       :value value}])))

(defn ->app-db
  "Update the client db of the target client with the given `value`
  for the given `keys-vector`"
  [ev-msg key-vectore value]
  (update-app-db (:uid ev-msg) key-vectore value))
