(ns ^{:clojure.tools.namespace.repl/load false} hellhound.system.store)


(defn valid-system?
  [value]
  (cond
    (nil? value) false
    :else true))


;; Main storage for system data.
(defonce system
  (atom {}
        :validator valid-system?))

(def store nil)

(defn get-system
  "A shortcut function for derefing `system`."
  []
  @system)


;; (defn set-system!
;;   "Sets the system of HellHound."
;;   [system-map]
;;   (reset! system system-map))

(defn set-system!
  [system-fn]
  (alter-var-root #'store (constantly (system-fn))))
