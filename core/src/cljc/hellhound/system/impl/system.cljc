(ns hellhound.system.impl.system
  {:added 1.0}
  (:require
   [clojure.spec.alpha         :as s]
   [hellhound.component        :as hcomp]
   [hellhound.system.protocols :as protocols]))


(defn conform-component
  "Checks for a valid compnoent structure and returns a pair of component
  name and the component structure."
  [component]
  (when (not (satisfies? hcomp/IComponent component))
    ;; Throw if component didn't satisfy the protocol.
    (throw (ex-info "Provided component does not satisfies `IComponent` protocol."
                    {:cause component})))

  (if (s/valid? :hellhound.component/component component)
    [(hcomp/get-name component) (hcomp/initialize component)]
    (throw (ex-info (format "Component does not satisfies '%s' spec."
                            ":hellhound.component/component")
                    {:cause (hcomp/get-name component)
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

  (make-components-map
    [this]
    {:components-map (into {} (map conform-component
                                   (protocols/components-vector this)))})

  protocols/WorkflowManagement
  (get-workflow [this]
    (:workflow this))

  protocols/SystemManagement
  (update-system
    [system k v]
    (assoc system k v)))
