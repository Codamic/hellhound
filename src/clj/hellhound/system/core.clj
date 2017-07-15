(ns hellhound.system.core
  "All the functions for managing system state live in this namespace")


;; Main storage for system data.
(def system (atom {}))

(defn get-system
  []
  @system)

(defn update-system!
  [fn]
  (swap! system fn))

(defn reset-system!
  [value]
  (reset! system value))

(defn get-system-entry
  [component-name]
  (get (:components (get-system)) component-name))
