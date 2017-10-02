(ns hellhound.system.core
  "All the functions for managing system state live in this namespace. You
  won't need to use this namespace directly unless you really know what's
  your doing."
  (:require
   [clojure.spec.alpha         :as s]
   [hellhound.component        :as comp]
   [hellhound.system.workflow  :as workflow]
   [hellhound.system.utils     :as utils]
   [hellhound.logger :as log])

  (:import
   (hellhound.component IComponent)
   (clojure.lang IPersistentMap
                 PersistentArrayMap
                 PersistentVector)))

;; Main storage for system data.
(def system (atom {}))

(defn context-for
  "Returns the context map for the given component in the
  system."
  [component]
  {})

(defn get-system
  "A shortcut function for derefing `system`."
  []
  @system)


(defn conform-component
  "Checks for a valid compnoent structure and returns a pair of component
  name and the component structure."
  [component]
  (when (not (satisfies? comp/IComponent component))
    ;; Throw if component didn't satisfy the protocol.
    (throw (ex-info "Provided component does not satisfies `IComponent` protocol."
                    {:cause component})))

  (if (s/valid? :hellhound.component/component component)
    [(comp/get-name component) (comp/initialize component)]
    ;; If component did not satisfies component spec
    (throw (ex-info "Component does not satisfies ':hellhound.component/component' spec."
                    {:cause (:hellhound.component/name component)
                     :explain (s/explain-data :hellhound.component/component component)}))))


(s/fdef hellhound.system.core/conform-component
        :args (s/cat :component :hellhound.component/component)
        :ret  vector?
        :fn #(= (:ret %) [(:hellhound.component/name (:component (:args %)))
                          (:component (:args %))]))

(defn ^IPersistentMap components-map
  "Returns a map of components from the given `system`. Basically
  the return value of this function would be index of components."
  [^IPersistentMap system-map]
  (into {} (map conform-component
                (utils/get-components system-map))))

(defn update-system-components
  "Replace the components vector of an unprocessed `system` with the indexed
  version of the vector which is map."
  [system-map]
  (merge system-map
         {:components (components-map system-map)
          :components-workflow (workflow/workflow-map system-map)}))

(defn set-system!
  "Sets the system of HellHound."
  [^IPersistentMap system-map]
  (reset! system (update-system-components system-map)))

(defn get-dependencies-of
  "Returns a vector of dependencies for the given `component` ins the given
  `system`."
  [system-map component]
  (let [dependencies (comp/dependencies component)]
    (filter #(some #{(comp/get-name %)} dependencies)
            (vals (utils/get-components system-map)))))

(defn ^IPersistentMap start-component!
  "Starts the given `component` of the given `system`."
  [^IPersistentMap system-map ^IComponent component]
  (if (comp/started? component)
    (do
      (log/debug "Component '"
                 (:hellhound.component/name component)
                 "' already started. Skipping it.")
      system-map)
    (update-in (reduce start-component! system-map
                       (get-dependencies-of system-map component))
               [:components (comp/get-name component)]
               ;; New value for the component name which will be the return
               ;; value of the `start-fn` function
               (fn [_]
                 (comp/start! component
                              (context-for component))))))


(defn stop-component!
  "Stops the given `component` of the given `system`."
  [^IPersistentMap system-map ^IComponent component]
  (if-not (comp/started? component)
    system-map
    (reduce stop-component!
            (update-in system-map
                       [:components (comp/get-name component)]
                       (fn [_] (comp/stop! component)))
            (get-dependencies-of system-map component))))

(s/def ::system-map (s/and map?
                           #(contains? % :components)
                           #(map? (:components %))))


(defn start-system!
  "Starts the given `system-map`.

  TODO: More explaination."
  {:public-api true
   :added      1.0}
  [^IPersistentMap system-map]
  (if-not (s/valid? ::system-map system-map)
    (throw (ex-info "Provided system is not valid" {:cause (s/explain ::system-map system-map)}))

    (reset! system
            (reduce start-component!
                    system-map
                    (vals (utils/get-components system-map))))))

(defn stop-system!
  "Stops the given `system-map`.

  TODO: More explaination"
  {:public-api true
   :added      1.0}
  [^IPersistentMap system-map]
  (reset! system
          (reduce stop-component!
                  system-map
                  (vals (utils/get-components system-map)))))
