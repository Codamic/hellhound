(ns hellhound.system.core
  "All the functions for managing system state live in this namespace"
  (:require
   [clojure.spec.alpha         :as s]
   [hellhound.component        :as comp])

  (:import (clojure.lang IPersistentMap
                         PersistentArrayMap
                         PersistentVector)))

;; Main storage for system data.
(def system (atom {}))

(defn get-system
  []
  @system)

(s/fdef get-components
        :args (s/cat :system map?)
        :ret vector?
        :fn #(= (:ret %) (-> :args :system :components)))

(defn ^PersistentVector get-components
  "Returns the components catalog of the given `system`."
  [^IPersistentMap system]
  (:components system))

(defn conform-component
  [component]
  (if (satisfies? comp/IComponent component)
    [(comp/get-name component) component]
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
  [^IPersistentMap system-map]
  (reset! system (update-system-components system-map)))

(defn get-dependencies-of
  [system-map component]
  (let [dependencies (comp/dependencies component)]
    (filter #(some #{(comp/get-name %)} dependencies)
            (vals (get-components system-map)))))

(defn start-component!
  "Starts the given `component` of the given `system`."
  [system-map component]
  (if (comp/started? component)
    system-map
    (update-in (reduce start-component! system-map
                       (get-dependencies-of system-map component))
               [:components (comp/get-name component)]
               (fn [_] (comp/start! component {})))))


(defn stop-component!
  [system-map component]
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
  "Starts the given `system-map`."
  [system-map]
  (if-not (s/valid? ::system-map system-map)
    (throw (ex-info "Provided system is not valid" {:cause (s/explain ::system-map system-map)}))
    (reduce start-component! system-map (vals (get-components system-map)))))

(defn stop-system!
  [system-map]
  (reduce stop-component! system-map (vals (get-components system-map))))
