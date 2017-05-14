(ns hellhound.tasks.core
  (:require [clojure.java.io :as io]))

(def project-path (System/getProperty "user.dir"))
(def project-dir  (io/file project-path))

(def migration-dir (io/file project-path "src/db/migrations"))

(defn in-migrations
  [path]
  (io/file migration-dir path))
