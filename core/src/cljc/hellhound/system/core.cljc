(ns hellhound.system.core
  "All the functions for managing system state live in this namespace. You
  won't need to use this namespace directly unless you really know what's
  your doing."
  (:require
   [clojure.spec.alpha             :as s]
   [hellhound.logger               :as log]
   [hellhound.components.protocols :as cimpl]
   [hellhound.streams              :as streams]
   [hellhound.system.workflow      :as workflow]
   [hellhound.system.utils         :as utils]
   [hellhound.system.impl.system   :as sysimpl]
   [hellhound.system.protocols     :as impl]
   [hellhound.logger               :as logger])

  (:import
   [hellhound.components.protocols IComponent]
   [clojure.lang
    IPersistentMap
    PersistentArrayMap
    PersistentVector]))


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

  (let [components   (impl/components-map system-map)
        dependencies (cimpl/dependencies component)
        deps         (map #(get components %) dependencies)]
    (when (nil? components)
      (throw (ex-info "Components map is nil. Did you set the system?"
                      {:cause system-map})))

    {:dependencies deps
     :dependencies-map (into {} (map (fn [x] [(cimpl/get-name x) x]) deps))}))


(defn make-components-index
  "Creates an index from components vector of the `system` and store it
  under `:component-map` key in system."
  [system-map]
  (merge system-map
         (impl/make-components-map system-map)))


(defn init-system
  "Initializes the given `system-map` by making an index from components and
  returns the new system."
  [system-map]
  (make-components-index system-map))


(defn get-dependants-of
  "Returns a list of components of the given system which are depends on the
   given component."
  [system component]
  (let [component-name (cimpl/get-name component)]
    (filter #(some #{component-name} (cimpl/dependencies %))
            (vals (impl/components-map system)))))


(defn get-dependencies-of
  "Returns a vector of dependencies for the given `component` in the given
  `system`."
  [^IPersistentMap system-map component]
  (let [dependencies (cimpl/dependencies component)]
    (filter #(some #{(cimpl/get-name %)} dependencies)
            (vals (impl/components-map system-map)))))


(defn ^IPersistentMap start-component!
  "Starts the given `component` of the given `system`."
  [^IPersistentMap system-map
   ^IComponent     component]
  (let [dependencies (get-dependencies-of system-map component)
        new-system   (reduce start-component! system-map dependencies)]
    (update-in new-system
               [:components-map (cimpl/get-name component)]
               ;; New value for the component name which will be the return
               ;; value of the `start-fn` function
               (fn [old-component]
                 (cimpl/start! old-component
                               (context-for new-system old-component))))))


(defn stop-component!
  "Stops the given `component` of the given `system`."
  [^IPersistentMap system-map
   ^IComponent     component]
  (reduce stop-component!
          (update-in system-map
                     [:components-map (cimpl/get-name component)]
                     (fn [old-component]
                       (streams/close! (cimpl/input old-component))
                       (streams/close! (cimpl/output old-component))
                       ;; TODO: Should we call stop as the last step ?
                       (cimpl/stop! old-component)))
          (get-dependencies-of system-map component)))

(s/def ::system-map (s/and map?
                           #(contains? % :components)
                           #(vector? (:components %))))


(defn start-system
  "Starts the given `system-map`.

  The given system should contains a vector of components under `:components`
  key and a vector of workflow defination under `:workflow` key.

  In order to start the system. HellHound creates a dependency tree form
  the components and call start function on all of them. After starting
  all the components, HellHound will setup the workflow and data pipeline
  according to description in `:workflow`."
  {:public-api true
   :added      1.0}
  [^IPersistentMap system-map]
  (if-not (s/valid? ::system-map system-map)
    (throw (ex-info "Provided system is not valid"
                    {:cause (s/explain-data ::system-map system-map)})))

  (reduce start-component!
          system-map
          (vals (impl/components-map system-map))))


(defn stop-system
  "Stops the given `system-map` by calling stop function of all the
  components with respect to dependency tree."
  {:public-api true
   :added      1.0}
  [^IPersistentMap system-map]
  (reduce stop-component!
          system-map
          (vals (impl/components-map system-map))))

(defn shutdown-hook
  [system]
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. (fn []
              (logger/info "Shutting down...")
              (stop-system system)))))
