(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [in-migrations]]
            [clojure.string       :refer [ends-with? starts-with?]]
            [clj-time.core        :refer [before?]]))


(defn long-str [& strings] (clojure.string/join "\n" strings))
(def migration_template
  (long-str "(ns db.migrations.%s)"
            ""
            "(defn up"
            "  []"
            "  ;; Your migration code goes here"
            ")"
            ""
            "(defn down"
            "  []"
            "  ;; Your code to teardown this migration, goes here"
            ")"))

(defn- is-timestamp?
  [value]
  true)

(defn- timestamp
  [namespace]
  (let [timestamp (:timestamp (meta namespace))
        nsname   (ns-name namespace)]

    (if (is-timestamp? timestamp)
      timestamp
      (throw (Exception.
              (format "Timestamp of '%s' namespace is not valid" nsname))))))

(defn find-migrations
  []
  (let [namespaces (all-ns)]
    (->> namespaces
         (filter (fn [x] (println (ns-name x)) (starts-with? (ns-name x) "db.migrations")))
         (sort #(before? (timestamp %1) (timestamp %2))))))


(defn epoch-time
  []
  (quot (System/currentTimeMillis) 1000))

(defn migrate
  [& rest]
  (let [migrations (find-migrations)]
    (if (empty? migrations)
      (println "No migration found")
      (doseq [migration migrations]
        (println migration)))))

(defn new-migrate
  [name & rest]
  (println name)
  (let [epoch     (epoch-time)
        nsname    (str epoch "_" name)
        file-path (in-migrations (str nsname ".clj"))]
    (println epoch)
    (println nsname)
    (println file-path)
    (spit file-path
          (format migration_template nsname))))

(defn wrong-command
  [cmd]
  (println (format "Can't find the command '%s'." cmd)))

(defn -main
  [command & rest]
  (println command)
  (println rest)
  (println "<>")
  (cond
    (= command "migration") (apply new-migrate rest)
    (= command "migrate")   (apply migrate rest)
    :else (wrong-command command)))
