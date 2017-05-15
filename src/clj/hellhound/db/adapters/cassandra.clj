(ns hellhound.db.adapters.cassandra
  (:require [hellhound.db.adapters.core :refer [DatabaseAdapter]]))


(defrecord CassandraAdaptor [session]
  DatabaseAdapter
  (create-table
    [name]
    (create-keyspace session name
                     (if-exists false)
                     (with {:replication
                            {"class" "SimpleStrategy"
                             "replication_factor" 1}})))
  (insert-migration
    [migration status]
    (insert session name {:migration-name migration
                          :status status})))
