(ns hellhound.system.store
  "This namespace contains all the necessary means to store and manage the only
  global state of a **HellHound** application which is the `system` description.

  `store` is the binding which holds the system. In order to set the system, use
  `set-system!` function which alters the root of `store` binding to point to the
  given system."
  {:clojure.tools.namespace.repl/load false})


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
