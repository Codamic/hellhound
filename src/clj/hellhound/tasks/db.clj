(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [info error warn]]
            [clojure.edn          :as edn]
            [joplin.repl          :as joplin]
            [environ.core         :refer [env]]))

(defn- get-joplin-config
  []
  )

(defn migrate
  "Start the migration process"
  [& rest]
  )

(defn new-migrate
  "Create a new migration file with the given name"
  [name & rest]
  )

(defn wrong-command
  [cmd]
  (error (format "Can't find the command '%s'." cmd)))


(defn -main
  [command & rest]
  (get-lock)
  (cond
    (= command "migration") (apply new-migrate rest)
    (= command "migrate")   (apply migrate rest)
    :else (wrong-command command)))
