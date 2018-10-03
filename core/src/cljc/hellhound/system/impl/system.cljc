(ns hellhound.system.impl.system
  {:added 1.0}
  (:require
   [clojure.spec.alpha         :as s]
   [hellhound.components.protocols :as cimpl]
   [hellhound.system.protocols :as protocols]
   [hellhound.system.execution :as exec]))

(defn conform-component
  "Checks for a valid compnoent structure and returns a pair of component
  name and the component structure."
  [component]
  (when-not (satisfies? cimpl/IComponent component)
    ;; Throw if component didn't satisfy the protocol.
    (throw (ex-info "Provided component does not satisfies `IComponent` protocol."
                    {:cause component})))

  (if (s/valid? :hellhound.component/component component)
    [(cimpl/get-name component) (cimpl/initialize component)]
    (throw (ex-info (format "Component does not satisfies '%s' spec."
                            ":hellhound.component/component")
                    {:cause (cimpl/get-name component)
                     :explain (s/explain-data
                               :hellhound.component/component
                               component)}))))


(s/fdef hellhound.system.core/conform-component
        :args (s/cat :component :hellhound.component/component)
        :ret  vector?
        :fn #(= (first (:ret %))
                (:hellhound.component/name (:component (:args %)))))

(extend-type clojure.lang.IPersistentMap
  protocols/ComponentManagement
  ;; The system map should have a `:component` key which its value
  ;; is a vector of component.
  (components-vector
    [this]
    (:components this))

  (components-map
    [this]
    (:components-map this))

  (get-component
    [this component-name]
    (let [components-map (protocols/components-map this)]
      (when components-map
        (get components-map component-name))))

  (update-component
    [system component-name component]
    (protocols/update-system system
                             :components-map
                             (assoc (protocols/components-map system)
                                    component-name
                                    component)))

  (make-components-map
    [this]
    {:components-map (into {} (map conform-component
                                   (protocols/components-vector this)))})

  protocols/WorkflowManagement
  (get-workflow
    [this]
    (:workflow this))

  protocols/ExecutionManagement
  (execution-pool
    [system]
    (cond
      (exec/single-threaded? system) nil
      (exec/multi-threaded? system)  (or (:execution-pool (exec/execution-map system))
                                         @(exec/default-execution-pool system))
      :else (throw (ex-info "Don't know about the given execution mode." {}))))

  (wait-pool
    [system]
    (cond
      (exec/single-threaded? system) nil
      (exec/multi-threaded? system)  (or (:wait-pool (exec/execution-map system))
                                         @(exec/default-wait-pool system))
      :else (throw (ex-info "Don't know about the given execution mode." {}))))

  (schedule-pool
    [system]
    (cond
      (exec/single-threaded? system) nil
      (exec/multi-threaded? system)  (or (:schedule-pool (exec/execution-map system))
                                         @(exec/default-schedule-pool system))
      :else (throw (ex-info "Don't know about the given execution mode." {}))))

  (execution-mode
    [system]
    (or (:mode (exec/execution-map system))
        :single-thread))

  protocols/SystemManagement
  (update-system
    [system k v]
    (assoc system k v))

  (get-value
    ([system ks]
     (get-in system ks))
    ([system ks default-value]
     (get-in system ks default-value))))
