(ns hellhound.system.core
  "All the functions for managing system state live in this namespace. You
  won't need to use this namespace directly unless you really know what's
  your doing."
  (:require
   [clojure.spec.alpha         :as s]
   [hellhound.system.workflow  :as workflow]
   [hellhound.system.utils     :as utils]
   [hellhound.logger :as log]
   [hellhound.component :as hcomp])

  (:import
   (hellhound.component IComponent)
   (clojure.lang IPersistentMap
                 PersistentArrayMap
                 PersistentVector)))

;; Main storage for system data.
(def system (atom {}))

(defn context-for
  "Returns the `context map` for the given component in the given
  `system-map`.

  Basically concext map contains the following keys:

  * `:dependencies`: A vector of running components which the current component
    is depends on.

  * `:dependencies-map`: A map of component names as keys and running components
    as values. All the components are the dependencies of the given `component`.

  NOTE: for more info checkout the guides for `Context Map`."
  [system-map component]

  (let [components   (:components system-map)
        dependencies (hcomp/dependencies component)
        deps         (map #(get components %) dependencies)]
    {:dependencies deps
     :dependencies-map (into {} (map (fn [x] [(hcomp/get-name x) x]) deps))}))


(defn get-system
  "A shortcut function for derefing `system`."
  []
  @system)


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
    ;; If component did not satisfies component spec
    (throw (ex-info "Component does not satisfies ':hellhound.component/component' spec."
                    {:cause (hcomp/get-name component)
                     :explain (s/explain-data :hellhound.component/component component)}))))


(s/fdef hellhound.system.core/conform-component
        :args (s/cat :component :hellhound.component/component)
        :ret  vector?
        :fn #(= (first (:ret %)) (:hellhound.component/name (:component (:args %)))))

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
         {:components (components-map system-map)}))

(defn set-system!
  "Sets the system of HellHound."
  [^IPersistentMap system-map]
  (reset! system (update-system-components system-map)))

(defn get-dependencies-of
  "Returns a vector of dependencies for the given `component` ins the given
  `system`."
  [system-map component]
  (let [dependencies (hcomp/dependencies component)]
    (filter #(some #{(hcomp/get-name %)} dependencies)
            (vals (utils/get-components system-map)))))

(defn ^IPersistentMap start-component!
  "Starts the given `component` of the given `system`."
  [^IPersistentMap system-map ^IComponent component]
  (let [dependencies (get-dependencies-of system-map component)
        new-system   (reduce start-component! system-map dependencies)]
    (update-in new-system
               [:components (hcomp/get-name component)]
               ;; New value for the component name which will be the return
               ;; value of the `start-fn` function
               (fn [old-component]
                 (hcomp/start! old-component
                               (context-for new-system old-component))))))

(defn stop-component!
  "Stops the given `component` of the given `system`."
  [^IPersistentMap system-map ^IComponent component]
  (reduce stop-component!
          (update-in system-map
                     [:components (hcomp/get-name component)]
                     (fn [old-component] (hcomp/stop! old-component)))
          (get-dependencies-of system-map component)))

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
    (throw (ex-info "Provided system is not valid"
                    {:cause (s/explain-data ::system-map system-map)})))

  (reset! system
          (reduce start-component!
                  system-map
                  (vals (utils/get-components system-map))))
  (log/info "System started successfully."))

(defn stop-system!
  "Stops the given `system-map`.

  TODO: More explaination"
  {:public-api true
   :added      1.0}
  [^IPersistentMap system-map]
  (reset! system
          (reduce stop-component!
                  system-map
                  (vals (utils/get-components system-map))))
  (log/info "System stopped successfully."))


(defn get-supervisor
  [system]
  (:supervisor system))

(defn setup-supervisor
  [system]
  (if-let [supervisor (get-supervisor system)]
    (hcomp/initialize supervisor)))

(defn start-supervisor
  [system]
  (if-let [supervisor (get-supervisor system)]
    (hcomp/start supervisor)))
