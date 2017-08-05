(ns hellhound.system.core
  "All the functions for managing system state live in this namespace"
  (:require
   [clojure.spec.alpha         :as s]
   [hellhound.system.protocols :as protocols]))

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


(defn for-each-component
  [components f]
  (doseq [component components]
    (f component)))


(defn start-dependencies
  [system component]
  ())

(defn start-component!
  [system component]
  (let [dependencies (dependencies component)]
    (map #(start-component)))
  (start-dependencies systbem component)
  (wire-component-io))


(defn start-system!
  [system]
  (for-each-component (components system)
                      #(start-component! system %)))



(s/fdef get-components
        :args (s/cat :system map?)
        :ret vector?
        :fn #(= (:ret %) (-> :args :system :components)))

(defn get-components
  "Returns the components catalog of the given `system`."
  [system]
  (:components system))

(extend-protocol protocols/System
  clojure.lang.PersistentArrayMap
  (start! [this]
    (:hellhound.component/start-fn this))

  (components [this] (get-components this))

  (get-component [this component-name]
    (component-name (components this))))
