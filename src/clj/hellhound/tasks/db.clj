(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [in-migrations epoch-time long-str info error warn]]
            [clojure.string       :refer [ends-with? starts-with?]]
            [clojure.edn          :as edn]
            [clj-time.core        :refer [before?]]
            [clojure.java.io :as io]))

;; DEFINATIONS

;; Lock is not a great name i know, but i don't have
;; time to think about it
(def migrations-lock (atom []))
(def migration-lock-file (io/resource "migration.lock"))
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

;; FUNCTIONS
(defn get-lock
  "Get the migrations listed in the `migration.lock` file and reset the
  global `migrations-lock` atom with the exist value. If there were
  no migrations listed will ignore resetting the atom."
  []
  (if (nil? migration-lock-file)
    (throw (Exception. "Can't find migration.lock file inside resource path."))
    (let [migrations (edn/read-string (slurp migration-lock-file))]
      (if-not (empty? migrations)
        (reset! migrations-lock migrations)))))

(defn update-lock
  "Update the global `migrations-lock` atom with the given `nsname`
  and flush the changes into `migration.lock` file."
  [nsname]
  (let [migrations (conj @migrations-lock nsname)]
    (spit migration-lock-file (pr-str migrations))
    (swap! migrations-lock (fn [_] migrations))))

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

(defn migrate
  [& rest]
  (let [migrations @migrations-lock]
    (if (empty? migrations)
      (warn "No migration found")
      (doseq [migration migrations]
        (println migration)))))

(defn new-migrate
  [name & rest]
  (let [epoch     (epoch-time)
        nsname    (str epoch "_" name)
        file-path (in-migrations (str nsname ".clj"))]
    (info "Creating migration file: " file-path)
    (spit file-path
          (format migration_template nsname))
    (info "Updating migrations lock file")
    (update-lock nsname)))

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
