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

(s/fdef get-components
        :args (s/cat :system map?)
        :ret vector?
        :fn #(= (:ret %) (-> :args :system :components)))

(defn ^ISeq get-components
  "Returns the components catalog of the given `system`."
  [^IPersistentMap system]
  (:components system))

(defn conform-component
  [component]
  (if (satisfies? IComponent component)
    [(get-name component) component]
    ;; Throw if component didn't satisfy the protocol.
    (ex-info "Provided component does not satisfies `IComponent` protocol."
             {:cause component})))

(defn ^IPersistentMap components-map
  [^IPersistentMap system-map]
  (into {} (map conform-component
                (get-components system-map))))

(defn update-system-components
  [system-map]
  (merge system-map
         {:components (components-map system-map)}))

(defn set-system!
  "Sets the system of HellHound."
  [^IPersistentMap]
  (reset! system (update-system-components)))

(defn get-dependencies-of
  [system-map component]
  (let [dependencies (dependencies component)]
    (filter #(some #{(get-name %)} dependencies)
            (vals (get-components system-map)))))

(defn start-component!
  "Starts the given `component` of the given `system`."
  [system-map component]
  (if (started? component)
    system-map
    (update-in (reduce start-component! system-map
                       (get-dependencies-of system-map component))
               [:components (get-name component)]
               (fn [_] (start! component {})))))


(defn stop-component!
  [system-map component]
  (if-not (started? component)
    system-map
    (reduce stop-component!
            (update-in system-map
                       [:components (get-name component)]
                       (fn [_] (stop! component)))
            (get-dependencies-of system-map component))))

(s/def ::system-map (s/and map?
                           #(contains? % :components)
                           #(map? (:components %))))


(defn start-system!
  "Starts the given `system-map`."
  [system-map]
  (if-not (s/valid? ::system-map system-map)
    (throw (ex-info "Provided system is not valid" {:cause (s/explain ::system-map system-map)}))
    (reduce start-component! system-map (vals (get-components system-map)))))

(defn stop-system!
  [system-map]
  (reduce stop-component! system-map (vals (get-components system-map))))
