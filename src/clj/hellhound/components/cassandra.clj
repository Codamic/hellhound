(ns hellhound.components.cassandra
  "This namespace provides the necessary means to communicate with
  database."
  (:require [environ.core                   :as environ]
            [qbits.alia                     :as alia]
            [hellhound.components.core      :as component]
            [hellhound.logger.core          :as logger]
            [hellhound.core                 :refer [application-config]]))


(defn- cassandra-config
  []
  (:cassandra (:db (application-config))))

(defn- connect
  ([]
   (connect {}))
  ([config]
   (let [conf    (merge (cassandra-config) config)
         cluster (alia/cluster conf)]
     (alia/connect cluster))))


(defrecord Cassandra [options]
  component/Lifecycle
  (start [this]
    (logger/info "Connecting to Cassandra cluster...")
    (assoc this :session (connect options)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        (alia/shutdown (:session this))
        (dissoc this :session))
      this)))


(defn make-cassandra-client
  "Create an instance of `Cassandra` record to be used with a `component`
  compatible system."
  ([]
   (make-cassandra-client {}))
  ([options]
   (->Cassandra options)))

(defn cassandra-client
  "Create an instance from cassandra component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (cassandra-client system-map {}))
  ([system-map options]
   (update-in system-map [:components :cassandra] (->Cassandra options))))
