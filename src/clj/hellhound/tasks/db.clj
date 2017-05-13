(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [migration-dir]]
            [clojure.string       :refer [ends-with? starts-with?]]
            [clj-time.core        :refer [before?]]))


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
  (-> (all-ns)
         (filter #(starts-with? (ns-name %) "db.migrations"))
         (sort #(before? (timestamp %1) (timestamp %2)))))

(defn -main
  [& rest]
  (println ":)")
  (let [migrations (find-migrations)]
    (doseq [migration migrations]
      (println (pr-str migration)))))
