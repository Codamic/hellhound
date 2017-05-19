(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [info error warn]]
            [clojure.edn          :as edn]
            [joplin.repl          :as joplin]
            [environ.core         :refer [env]]
            [clojure.java.io      :as io]))

(defn- joplin-config
  []
  (joplin/load-config (io/resource "database.edn")))

(defn- current-env
  []
  :dev)

(defn migrate
  "Start the migration process"
  [config & rest]
  (joplin/migrate config (current-env)))

(defn new-migrate
  "Create a new migration file with the given name"
  ([config name]
   (new-migrate config (:default-db config) name))
  ([config db name & rest]
   (println "<<<<")
   (println config)
   (println (current-env))
   (println db)
   (println name)
   (joplin/create config (current-env) (keyword db) name)))

(defn wrong-command
  [cmd]
  (error (format "Can't find the command '%s'." cmd)))


(defn -main
  [command & rest]
  (let [config (joplin-config)]
    (cond
      (= command "migration") (apply new-migrate config rest)
      (= command "migrate")   (apply migrate config rest)
      :else (wrong-command command))))
