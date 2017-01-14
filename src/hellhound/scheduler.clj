(ns hellhound.scheduler
  "A wrapper around the immutant scheduling namespace."
  (:require [immutant.scheduling :as s]))


(def schedule s/schedule)
(def stop     s/stop)
