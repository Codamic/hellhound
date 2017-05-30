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
(spec/def ::cassanda-configuration
  (spec/keys :req [::connection ::keyspace]))


;; Private functions ---------------------------------------
(defn- connect
  [config]
  (let [cluster (alia/cluster (:connecttion config))]
    (alia/connect cluster)))

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
      (throw
       (ex-info "Keyspace is not present. You need to migrate first." {})))))

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
    (let [session   (connect options)
          keyspace  (:name (:keyspace options))]
      (select-keyspace session keyspace)
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
