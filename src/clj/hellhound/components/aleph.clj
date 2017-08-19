(ns hellhound.components.aleph
  (:require
   [aleph.http :as http]
   [hellhound.logger :as log]
   [hellhound.core :as hellhound]))


(defn aleph-start!
  "Returns a function to start the aleph server from given
  `routes` and `address` map"
  [routes address]
  (fn [this context]
    (log/info "Starting aleph server ...")
    (assoc this :instance (http/start-server routes address))))

(defn aleph-stop!
  "Stops the running aleph server"
  [this]
  (log/info "stopping system")
  (if (:instance this)
    (do
      (.close (:instance this))
      (dissoc this :instance))
    this))


(defn aleph-factory
  ([routes]
   (aleph-factory routes (hellhound/get-config :http)))
  ([routes address]
   {:hellhound.component/name ::aleph
    :hellhound.component/start-fn (aleph-factory routes address)
    :hellhound.component/stop-nf stop-aleph}))
