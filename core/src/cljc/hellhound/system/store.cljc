(ns hellhound.system.store
  (:import
   (clojure.lang IPersistentMap)))

(defn valid-system?
  [value]
  (cond
    (nil? value) false
    :else true))

;; Main storage for system data.
(def system
  (atom {}
        :validator valid-system?))

(defn get-system
  "A shortcut function for derefing `system`."
  []
  @system)


(defn set-system!
  "Sets the system of HellHound."
  [^IPersistentMap system-map]
  (reset! system system-map))
