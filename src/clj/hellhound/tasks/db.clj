(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [in-migrations epoch-time long-str
                                          info error warn migration-prefix]]

            [clojure.string       :refer [ends-with? starts-with?]]
            [clojure.edn          :as edn]
            [clj-time.core        :refer [before?]]
            [clojure.java.io :as io]))

;; DEFINATIONS

;; Lock is not a great name i know, but i don't have
;; time to think about it
(def migrations-lock (atom []))
(def migration-lock-file (io/resource "migration.lock"))

(def migration-template
  (long-str "(ns %s.%s)"
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

(defn gen-nsname
  [migration]
  (symbol (format "%s.%s" migration-prefix migration)))

(defn up
  "load the given migration and call up function from it"
  [migration]
  (info (format "Running the migration file: %s" migration))
  (require (gen-nsname migration))
  (let [up-fn (ns-resolve (gen-nsname migration) 'up)]
    (up-fn)))

(defn migrate
  [& rest]
  (let [migrations @migrations-lock]
    (if (empty? migrations)
      (warn "No migration found")
      (doseq [migration migrations]
        (up migration)))))

(defn make-file-path
  [nsname]
  (in-migrations (clojure.string/replace
                  (str nsname ".clj")
                  #"-" "_")))

(defn new-migrate
  [name & rest]
  (let [epoch     (epoch-time)
        nsname    (str  name "_" epoch)
        file-path (make-file-path nsname)]

    (info "Creating migration file: " file-path)
    (spit file-path
          (format migration-template
                  migration-prefix
                  (clojure.string/replace nsname #"_" "-")))

    (info "Updating migrations lock file")
    (update-lock (clojure.string/replace nsname #"_" "-"))))

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
