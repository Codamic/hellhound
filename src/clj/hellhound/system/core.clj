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



(defn starting-context
  "Generates the `context` map to be passed to `start!` of the component"
  [component]
  {})

(defn call-start
  "Calls `start!` function of the component and generates the context for it."
  [component]
  (when-not (started? component)
    (start! component (starting-context component))))

(defn component-dependencies
  "Returns a vector of all the components of the `system` which the given
  `component` depends on them."
  [system component]
  (map #(get-component system %
                       (dependencies component))))

(defn with-component-dependencies
  "Runs the given function for all the dependencies of the given `component`
  and the `component` itself and update the given `system` with the result
  of the execution."
  [system component f]
  (let [components-map  (conj (component-dependencies system component)
                              component)]
    (update-system system
                   :components
                   (map f components-map))))

(defn start-component!
  "Starts the given `component` of the given `system`."
  [system component]
  (with-component-dependencies system component call-start))


(defn stop-component!
  "Stops the given `component` of the given `system`."
  [system component]
  (with-component-dependencies system component call-stop))

(defn start-system!
  "Starts the given `system` map."
  [system]
  (map #(start-component! system %) (components system)))


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
