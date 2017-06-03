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


(declare ->Cassandra)

;; Specs ---------------------------------------------------
(spec/def ::keyspace
  (spec/keys :req [::name ::replication]))

(spec/def ::cassanda-configuration
  (spec/keys :req [::connection ::keyspace]))

;; Private functions ---------------------------------------
(defn- connect
  [config]
  (let [cluster (alia/cluster (:connecttion config))]
    (try
      (alia/connect cluster)
      (catch com.datastax.driver.core.exceptions.NoHostAvailableException e
        (throw (ex-info "Can't connect to Cassandra Cluster." {}))))))

(defn- check-session
  [session name]
  (if (nil? session)
    (throw (ex-info
            (format "'%s' component is not started yet." name)
            {:cause "Cassandra component"}))))


;; Public functions ----------------------------------------
(defn select-keyspace
  "Use the given keyspace name or throw an exception if the keyspace was
  missing"
  [session keyspace]
  (try
    (alia/execute session (format "USE %s;" keyspace))
    (catch clojure.lang.ExceptionInfo e
      (let [err-msg (.getMessage (:exception (ex-data e)))]
        (if (clojure.string/ends-with? err-msg "does not exist")
          (do (logger/warn err-msg)
              (logger/warn "You need to run the migrations first."))
          (logger/error err-msg))))))

(defn cassandra-config
  "Returns the cassandra configuration"
  []
  (:cassandra (:db (hellhound/application-config))))


(defn make-cassandra-client
  "Create an instance of `Cassandra` record to be used with a `component`
  compatible system."
  ([]
   (make-cassandra-client {}))
  ([options]
   (let [config (merge (cassandra-config) options)]
     (->Cassandra config))))

(defn new-cassandra-client
  "Create an instance from cassandra component. This function is meant
  to be used with `hellhound.system.defsystem` macro."
  ([system-map]
   (new-cassandra-client system-map {}))
  ([system-map options]
   (let [config (merge (cassandra-config) options)]
     (update-in system-map [:components :cassandra] (->Cassandra config)))))


;; Component record ----------------------------------------
(defrecord Cassandra [options]

  ;; Lifecycle implementation.
  protocols/Lifecycle

  (start [this]
    ;; Validate Configuration
    (spec/valid? ::cassanda-configuration options)
    (logger/info "Connecting to Cassandra cluster...")

    ;; Connect to the cluster and select the default keyspace
    (let [session          (connect options)
          keyspace         (:name (:keyspace options))]
      (select-keyspace session keyspace)
      (logger/info "Connected to cassandra cluster.")
      (assoc this :session session :keyspace keyspace)))

  (stop [this]
    (if (:session this)
      (do
        (logger/info "Disconnecting from Cassandra cluster...")
        ;; Shutting down the connection to cluster
        (alia/shutdown (:session this))
        (dissoc this :session :keyspace :keyspace))
      this))

  ;; DatabaseLifecycle implementation. Allow the migration system to operate
  ;; with this component
  protocols/DatabaseLifecycle

  (setup [this storage-name]
    (let [session (:session this)]
      (check-session session "Cassandra")

      (let [keyspace-config (:keyspace (:options this))
            replication     {:replication (:replication keyspace-config)}]

        (logger/info "Creating the keyspace...")
        (alia/execute
         session
         (hayt/create-keyspace (:name keyspace-config)
                               (hayt/if-exists false)
                               (hayt/with replication)))

        (logger/info "Creating migration storage...")
        (alia/execute
         session
         (hayt/create-table
          storage-name
          (hayt/if-exists false)
          (hayt/column-definitions {:name :varchar
                                    ;; int because we use epoch time
                                    :timestamp :int
                                    :applies :Boolean
                                    :primary-key [:name :timestamp]})))

        (assoc this :keyspace (:name keyspace-config)))))

  (teardown [this])
  (submit-migration [this record]))
