(ns hellhound.system.execution
  (:require
   [hellhound.system.protocols :as impl]))

(defn execute-in
  [executor f]
  (.execute executor f))

(defn execute-with-system
  [system f]
  (execute-in (impl/execution-pool system) f))

(defn execute-io-with-system
  [system f]
  (execute-in (impl/wait-pool system) f))

(defn schedule-with-system
  [system f details]
  (execute-in (impl/schedule-pool system) f))
