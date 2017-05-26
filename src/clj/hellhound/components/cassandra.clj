(ns hellhound.components.cassandra
  "This namespace provides the necessary means to communicate with
  database."
  (:require [environ.core                   :as environ]
            [qbits.alia                     :as alia]
            [hellhound.components.db        :as db]
            [hellhound.logger.core          :as logger]
            [hellhound.core                 :as hellhound]))


(defn- cassandra-config
  []
  (:cassandra (:db (hellhound/application-config))))

(defn- connect
  ([]
   (connect {}))
  ([config]
   (let [conf    (merge (cassandra-config) config)
         cluster (alia/cluster conf)]
     (alia/connect cluster))))

(defn- check-session
  [session name]
  (if (nil? session)
    (throw (Exception. (format "'%s' component is not started yet." name)))))

(defrecord Cassandra [options]
  db/DatabaseLifecycle

  (start [this]
    (logger/info "Connecting to Cassandra cluster...")
    (assoc this :session (connect options)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        (alia/shutdown (:session this))
        (dissoc this :session))
      this))

  (setup [this]
    (let [session (:session this)]
      (check-session session "Cassandra")
      (prn "SSSSSSSSSSSSS"))))


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
