(ns hellhound.system.execution)

(defn get-executor
  [system]
  (:executor system))

(defn get-execution-model
  [system]
  (:execution-model system))
