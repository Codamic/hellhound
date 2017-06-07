(ns hellhound.tasks.db
  (:require
   [clojure.edn               :as edn]
   [clojure.java.io           :as io]
   ;; Internals
   [hellhound.core            :as hellhound]
   [hellhound.tasks.core      :as core]
   [hellhound.components.core :as system]))

;; Definitions ---------------------------------------------
;; Lock is not a great name i know, but i don't have
;; time to think about it
(def migrations-lock (atom []))
(def migration-lock-file (io/resource "migration.lock"))

(def migration-template
  (core/long-str "(ns %s.%s)"
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


(def migration-storage-name "hellhound_migration_storage")

;; Functions -----------------------------------------------
(defn db-config
  "Returns database configuration from the current environment
  configuration file."
  []
  (:db (hellhound/application-config)))

;; NOTE: For now we have to force a certain place for migrations
;; (defn migration-path
;;   "Returns the migration path on filesystem "
;;   []
;;   (or (:migration-path (db-config))
;;       ))

(defn databases-to-migration
  []
  (vec (keys (db-config))))

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

(defn gen-nsname
  [migration]
  (symbol (format "%s.%s" core/migration-prefix migration)))

(defn up
  "load the given migration and call up function from it"
  [migration]
  (core/info (format "Running the migration file: %s" migration))
  (require (gen-nsname migration))
  (let [up-fn (ns-resolve (gen-nsname migration) 'up)]
    (up-fn)))

(defn make-file-path
  [nsname]
  (core/in-migrations
   (clojure.string/replace
    (str nsname ".clj")
    #"-" "_")))

(defn start-component
  [name]
  (-> @(:system (system/start-component name))
      :components
      (get name)
      :record))

(defn start-databases
  ""
  []
  (doseq [component (databases-to-migration)]
    (start-component component)))

(defn stop-databases
  "Stop the whole system"
  []
  (system/stop))

(defn with-active-system
  [db f]
  (let [component-record (system/get-component db)]
    (f component-record)))

(defn setup-db
  [db-name]
  (with-active-system db-name
    (fn [component]
      (.setup component migration-storage-name))))

(defn teardown-db
  [db-name]
  (core/info (format "Tearing down the '%s' database..." db-name))
  (with-active-system db-name
    (fn [component]
      (.teardown component))))

;; Command functions ---------------------------------------
(defn create
  "Calls the `setup` function of each database component provided in the
  environment configuration. Basically each key under the `:db` entry
  in the configuration file. The goal of this task is to create and setup
  the necessary db related stuff.

  The most important thing to remember is that the database key name in the
  configuration **should** match the component name in the system map."
  [& rest]
  (core/info "Creating databases...")
  (doseq [db (databases-to-migration)]
    (setup-db db)))


(defn destroy
  "Calls the `teardown` function of each database component provided in the
  environment configuration. Basically each key under the `:db` entry
  in the configuration file. The goal of this task is to create and setup
  the necessary db related stuff.

  The most important thing to remember is that the database key name in the
  configuration **should** match the component name in the system map."
  [& rest]
  (core/info "Dropping databases...")
  (doseq [db (databases-to-migration)]
    (teardown-db db))
  (core/info "Stopping the system..."))

(defn migrate
  "Run the migrations in order by calling `up` function of each
  namespace."
  [& rest]
  (let [migrations @migrations-lock]
    (if (empty? migrations)
      (core/warn "No migration found")
      (doseq [migration migrations]
        (up migration)))))

(defn new-migrate
  [name & rest]
  (let [epoch     (core/epoch-time)
        nsname    (str  name "_" epoch)
        file-path (make-file-path nsname)]

    (core/info "Creating migration file: " file-path)
    (spit file-path
          (format migration-template
                  core/migration-prefix
                  (clojure.string/replace nsname #"_" "-")))

    (core/info "Updating migrations lock file")
    (update-lock (clojure.string/replace nsname #"_" "-"))))

(defn wrong-command
  [cmd]
  (core/error (format "Can't find the command '%s'." cmd)))

(defn run-command
  "Run the given function with given args after starting the database
  components"
  [f args]

  ;; Start all the database components mentioned in environment's
  ;; configuration under the `:db` key
  (core/info "Starting all the database components...")
  (start-databases)
  (apply f args)
  ;; Stop all the running components
  (core/info "Stopping the system...")
  (stop-databases))

(defn main
  [command & rest]
  ;; Read and update the migration information from lock file
  (get-lock)

  ;; Dispatch the command
  (cond
    (= command "migration") (run-command new-migrate rest)
    (= command "migrate")   (run-command migrate rest)
    (= command "create")    (run-command create rest)
    (= command "destroy")   (run-command destroy rest)
    :else (wrong-command command)))
