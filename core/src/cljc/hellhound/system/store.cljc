(ns ^{:clojure.tools.namespace.repl/load false} hellhound.system.store)

;; Main storage for system data.
(def store nil)

(defn get-system
  "A shortcut function for derefing `system`."
  []
  store)

(defn set-system!
  [system-fn]
  (alter-var-root #'store (constantly (system-fn))))
