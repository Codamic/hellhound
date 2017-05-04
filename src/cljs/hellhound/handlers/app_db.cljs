(ns hellhound.handlers.app-db
  "This namespace provides several event handlers which directly
  manipulate the `app-db`. Such as:
  * `:app-db/update`
  * ..."
  (:require [re-frame.core :as re-frame]))

;; `app-db/update` event handler, update the value
;; of the given keys in the `app-db` with the given
;; value.
(re-frame/reg-event-db
 :app-db/update
 (fn [db [_ {:keys [keys value]}]]
   (update-in db keys (fn [_]  value))))

;; `app-db/append` event handler, update the value
;; of the given keys in the `app-db` with appending
;; value.
(re-frame/reg-event-db
 :app-db/append
 (fn [db [_ {:keys [keys value]}]]
   (update-in db keys
              (fn [old]
                (let [old-val (or old [])]
                  (concat old-val value))))))
