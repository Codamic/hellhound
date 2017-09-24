(ns hellhound.system.workflow
  "TODO"
  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn setup
  [system]
  (println "SYSTEM MAP" system))


(defn ^PersistentVector walk-graph
  [^IPersistentMap parsed-map ^PersistentVector pair]
  (assoc parsed-map
         (first pair)
         (conj (get parsed-map (first pair)) (second pair))))

(defn ^IPersistentMap workflow-map
  "Parse the workflow graph and returns a hashmap which keys
  are the components name and the values are inputs for that component."
  [^IPersistentMap system-map]
  (reduce walk-graph {} (:workflow system-map)))
