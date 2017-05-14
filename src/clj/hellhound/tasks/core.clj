(ns hellhound.tasks.core
  (:require [clojure.java.io :as io]
            [colorize.core   :refer [color]]))

(def project-path (System/getProperty "user.dir"))
(def project-dir  (io/file project-path))

(def migration-dir (io/file project-path "src/db/migrations"))

(defn epoch-time
  []
  (quot (System/currentTimeMillis) 1000))

(defn long-str
  "A quick function which makes writting multi-line strings easier."
  [& strings]
  (clojure.string/join "\n" strings))

(defn in-migrations
  [path]
  (io/file migration-dir path))

(defn info
  [& rest]
  (println (apply color :green rest)))

(defn error
  [& rest]
  (println (apply color :red rest)))

(defn warn
  [& rest]
  (println (apply color :yellow rest)))
