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
   [hellhound.core                 :as hellhound]
   [hellhound.spec                 :as hspec]))


(declare ->Cassandra)

;; Defs ----------------------------------------------------
(def table-schema
  {:name :varchar
                                  ;; int because we use epoch time
                                  :timestamp :int
                                  :applies :Boolean
                                  :primary-key [:name :timestamp]})

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
      [cluster (alia/connect cluster)]
      (catch com.datastax.driver.core.exceptions.NoHostAvailableException e
        (throw (ex-info "Can't connect to Cassandra Cluster." {}))))))

(defn- check-session
  [session name]
  (if (nil? session)
    (throw (ex-info
            (format "'%s' component is not started yet." name)
            {:cause "Cassandra component"}))))


(defn- create-keyspace
  [session name replication]
  (alia/execute session
                (hayt/create-keyspace name
                                      (hayt/if-exists false)
                                      (hayt/with replication))))

(defn- create-table
  [session name]
  (alia/execute
   session
   (hayt/create-table name
                      (hayt/if-exists false)
                      (hayt/column-definitions table-schema))))

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
    (hspec/validate ::cassanda-configuration options)
    (logger/info "Connecting to Cassandra cluster...")

    ;; Connect to the cluster and select the default keyspace
    (let [[cluster session]          (connect options)
          keyspace         (:name (:keyspace options))]

      ;; Select the keyspace if it exists.
      (select-keyspace session keyspace)

      (logger/info "Connected to cassandra cluster.")

      (assoc this
             :options options
             :cluster cluster
             :session session
             :keyspace keyspace)))

  (stop [this]
    (if (:session this)
      (let [session (:session this)
            cluster (:cluster this)]
        (logger/info "Disconnecting from Cassandra cluster...")
        ;; Shutting down the connection to cluster
        (alia/shutdown session)
        (alia/shutdown cluster)
        (dissoc this :session :cluster :keyspace))
      this))

  ;; DatabaseLifecycle implementation. Allow the migration system to operate
  ;; with this component
  protocols/DatabaseLifecycle

  (setup [this storage-name]
    (check-session (:session this) "Cassandra")
    (let [session         (:session this)
          keyspace-config (:keyspace    (:options this))
          replication     {:replication (:replication keyspace-config)}]

      (logger/info "Creating the keyspace...")
      (create-keyspace session (:name keyspace-config) replication)

      (logger/info "Creating migration storage...")
      (create-table session storage-name)

      (logger/info "Migration storage created.")
      (assoc this :keyspace (:name keyspace-config))))

  (teardown [this])
  (submit-migration [this record]))
