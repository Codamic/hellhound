(ns hellhound.components.cassandra
  "This namespace provides the necessary means to communicate with
  database."
  (:require [environ.core                   :as environ]
            [qbits.alia                     :as alia]
            [qbits.hayt                     :as hayt]
            [hellhound.components.protocols :as protocols]
            [hellhound.logger.core          :as logger]
            [hellhound.core                 :as hellhound]))


(defn- cassandra-config
  []
  (:cassandra (:db (hellhound/application-config))))

(defn- connect
  ([]
   (connect {}))
  ([config]
   (let [cluster (alia/cluster (:connecttion config))]
     (alia/connect cluster))))

(defn- check-session
  [session name]
  (if (nil? session)
    (throw (Exception. (format "'%s' component is not started yet." name)))))

(defrecord Cassandra [options]
  protocols/DatabaseLifecycle

  (start [this]
    (logger/info "Connecting to Cassandra cluster...")
    (let [use-query (alia/prepare "USE :keyspace")
          session   (connect options)
          keyspace  (:keyspace options)]
      (alia/execute session use-query {:values {:keyspace keyspace}})
      (assoc this :session session)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        (alia/shutdown (:session this))
        (dissoc this :session :keyspace :keyspace-selected))
      this))

  (setup [this]
    (let [session (:session this)
          use-query (alia/prepare "USE :keyspace")]
      (check-session session "Cassandra")

      (let [keyspace-config (:keyspace (:options this))]
        (alia/execute
         session
         (hayt/create-keyspace (hayt/if-exists false)
                               (:name keyspace-config)
                               (hayt/with (:details keyspace-config))))

        (assoc this :keyspace (:name keyspace-config))))))


(defn make-cassandra-client
  "Create an instance of `Cassandra` record to be used with a `component`
  compatible system."
  ([]
   (make-cassandra-client {}))
  ([options]
   (let [config (merge (cassandra-config) options)]
     (->Cassandra config))))

(defn cassandra-client
  "Create an instance from cassandra component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (cassandra-client system-map {}))
  ([system-map options]
   (let [config (merge (cassandra-config) options)]
     (update-in system-map [:components :cassandra] (->Cassandra config)))))
