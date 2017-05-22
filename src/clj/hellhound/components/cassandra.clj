(ns hellhound.components.cassandra
  "This namespace provides the necessary means to communicate with
  database."
  (:require [environ.core                   :as environ]
            [qbits.alia                     :as alia]
            [hellhound.components.core      :as component]
            [hellhound.logger.core             :as logger]))


(defn- connect
  [hosts opts]
  (let [options (merge {:contact-points [hosts]
                        :load-balancing-policy :default}
                       opts)
        cluster (alia/cluster options)]
    (alia/connect cluster)))

(defrecord Cassandra [hosts options]
  component/Lifecycle
  (start [this]
    (logger/info "Connecting to Cassandra cluster...")
    (assoc this :session (connect hosts options)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        (alia/shutdown (:session this))
        (dissoc this :session))
      this)))

(defn new-cassandra-client
  "Create a new instance of `CassandraClient` component."
  []
  (let [hosts ["127.0.0.1"]]
    (->Cassandra hosts {})))

(defn make-cassandra-client
  "Creates a cassandra component instance."
  ([]
   (new-cassandra-client))
  ([hosts]
   (make-cassandra-client hosts {}))
  ([hosts options]
   (->Cassandra hosts options)))


(defn cassandra-client
  "Create an instance from cassandra component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (cassandra-client system-map ["127.0.0.1"] {}))
  ([system-map hosts]
   (cassandra-client system-map hosts {}))
  ([system-map hosts options]
   (update-in system-map [:components :cassandra] (make-cassandra-client hosts options))))
