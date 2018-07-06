(ns hellhound.system.store
  (:import
   (clojure.lang IPersistentMap)))


;; Main storage for system data.
(def system (atom {}))


(defn get-system
  "A shortcut function for derefing `system`."
  []
  @system)


(defn set-system!
  "Sets the system of HellHound."
  [^IPersistentMap system-map]
  (reset! system system-map))
