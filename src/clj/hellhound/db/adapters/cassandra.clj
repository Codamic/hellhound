(ns hellhound.db.adapters.cassandra
  (:require [hellhound.db.adapters.core :refer [DatabaseAdapter]]))


(defrecord CassandraAdaptor [session keyspace-name table-name]
  DatabaseAdapter
  (create-db
    []
    (create-keyspace session keyspace-name
                     (if-exists false)
                     (with {:replication
                            {"class" "SimpleStrategy"
                             "replication_factor" 1}}))
    (use-keyspace session keyspace-name))

  (create-table
    []
     (create-table session :database_migrations
                        (column-definitions {:name         :varchar
                                             :status       :varchar
                                             :primary-key [:name, :status]})))

  (insert-migration
    [migration status]
    (insert session keyspace-name {:name   migration
                                   :status status}))

  (migrations
    []
    (select session :database_migrations)))
