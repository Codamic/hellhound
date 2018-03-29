(ns hellhound.kafka.core
  (:require [manifold.stream :as stream])
  (:import
   [java.util Properties]))

(defn ^Properties make-config
  "Create a Properties instance using the given CONFIG hashmap."
  [config]
  (let [props (Properties.)]
    (.putAll props config)
    props))
