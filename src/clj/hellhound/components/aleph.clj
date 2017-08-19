(ns hellhound.components.aleph
  "Aleph web/websocket server component.
  In order this use this component simply call the `factory` function."
  ^{:author "Sameer Rahmani (@lxsameer)"}
  (:require
   [clojure.spec.alpha :as s]
   [aleph.http         :as http]
   [hellhound.logger   :as log]
   [hellhound.core     :as hellhound]))

(s/def ::port (s/int-in 1 65535))
(s/def ::host string?)
(s/def ::aleph-config (s/keys :req [::host ::port]))

(defn aleph-start!
  "Returns a function to start the aleph server from given
  `routes` and `config` map"
  [routes config]
  (fn [this context]
    (log/info "Starting aleph server ...")
    (assoc this :instance (http/start-server routes config))))

(defn aleph-stop!
  "Stops the running aleph server"
  [this]
  (log/info "stopping system")
  (if (:instance this)
    (do
      (.close (:instance this))
      (dissoc this :instance))
    this))


(defn factory
  "Returns a new aleph component by given `routes` and optional `config`
  map. For details about `config` map checkout aleph docs."
  ([routes]
   (aleph routes (hellhound/get-config :http)))
  ([routes config]
   {:hellhound.component/name ::aleph
    :hellhound.component/start-fn (aleph-start1 routes config)
    :hellhound.component/stop-nf aleph-stop!}))
