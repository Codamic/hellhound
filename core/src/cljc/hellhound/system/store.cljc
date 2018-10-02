(ns ^{:clojure.tools.namespace.repl/load false} hellhound.system.store)

;; Main storage for system data.
(def store nil)

(defn get-system
  "A shortcut function for derefing `system`."
  []
  store)

(defn set-system!
  "Sets the global and default system by changing the root of the
  `store` binding using the given `system-fn` which is a function
  that returns a system-map."
  [system-fn]
  (alter-var-root #'store (constantly (system-fn))))


(comment
  (set-system! (fn [] {:example 4}))
  (get-system))
