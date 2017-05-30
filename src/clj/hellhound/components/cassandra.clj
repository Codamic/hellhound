(ns hellhound.components.cassandra
  "This namespace provides the necessary means to communicate with
  database."
  (:require
   [clojure.spec.alpha             :as spec]
   [environ.core                   :as environ]
   [qbits.alia                     :as alia]
   [qbits.hayt                     :as hayt]
   ;; Internals
   [hellhound.components.protocols :as protocols]
   [hellhound.logger.core          :as logger]
   [hellhound.core                 :as hellhound]))


(spec/def ::cassanda-configuration
  (spec/keys :req [::connection ::keyspace]))

(defn cassandra-config
  []
  (:cassandra (:db (hellhound/application-config))))

(defn- connect
  [config]
  (let [cluster (alia/cluster (:connecttion config))]
    (alia/connect cluster)))

(defn- check-session
  [session name]
  (if (nil? session)
    (throw (Exception. (format "'%s' component is not started yet." name)))))

(defrecord Cassandra [options]
  protocols/DatabaseLifecycle

  (start [this]
    (spec/valid? ::cassanda-configuration options)
    (logger/info "Connecting to Cassandra cluster...")
    (let [session   (connect options)
          keyspace  (:name (:keyspace options))]

      (try
        (alia/execute session (format "USE %s;" keyspace))
        (catch  com.datastax.driver.core.exceptions.InvalidQueryException e
          (throw (Exception. "Keyspace is not present. You need to migrate first."))))
      (assoc this :session session :keyspace keyspace)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        (alia/shutdown (:session this))
        (dissoc this :session :keyspace :keyspace-selected))
      this))

  (setup [this]
    (let [session (:session this)]
      (check-session session "Cassandra")

      (let [keyspace-config (:keyspace (:options this))]
        (alia/execute
         session
         (hayt/create-keyspace (hayt/if-exists false)
                               (:name keyspace-config)
                               (hayt/with (:details keyspace-config))))

        (assoc this :keyspace (:name keyspace-config)))))

  (teardown [this]))


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
