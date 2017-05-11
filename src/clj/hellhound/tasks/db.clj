(ns hellhound.tasks.db
  (:require [hellhound.tasks.core :refer [migration-dir]]
            [clojure.string       :refer [ends-with?]]))


(defn -main
  [& rest]
  (println ":)")
  (let [migration-files (file-seq migration-dir)]
    (doseq [file migration-files]
      (let [fname (.getName file)]
        (if (and (not (.isDirectory file))
                 (ends-with? fname ".clj"))
          (require (symbol fname))
          (println (.getName file)))))))
