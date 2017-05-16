(ns hellhound.components.cassandra
  "This namespace provides the necessary means to communicate with
  database."
  (:require [clojurewerkz.cassaforte.client :as client]
            [environ.core                   :as environ]
            [hellhound.components.core      :as component]
            [hellhound.logger.core             :as logger]))


(defrecord Cassandra [host]
  component/Lifecycle
  (start [this]
    (logger/info "Connecting to Cassandra cluster...")
    (assoc this :session (client/connect host)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        (client/disconnect (:session this))
        (dissoc this :session))
      this)))

(defn new-cassandra-client
  "Create a new instance of `CassandraClient` component."
  []
  (let [host (or (environ/env :cassandra-host)
                 ["127.0.0.1"])]
    (->CassandraClient host)))
