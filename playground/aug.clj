(ns aug
  (:require [clojure.spec.alpha :as s])
  (:import (clojure.lang IPersistentMap
                         PersistentArrayMap
                         ISeq)))



(defprotocol IComponent
  "This protocol defines a very basic component for hellhound system."
  (start! [component context]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started? [component]
    "Returns a `true` if component started and `false` otherwise.")

  (get-name [component]
    "Returns the name of the component.")

  (dependencies [component]
    "Returns a vector of dependency names."))

(extend-protocol IComponent
  PersistentArrayMap
  (start! [this context]
    (let [start-fn (::start-fn this)]
      (assoc (start-fn this context)
             ::started? true)))

  (stop! [this]
    (let [stop-fn (::stop-fn this)]
      (assoc (stop-fn this) ::started? false)))

  (started? [this]
    (or (::started? this) false))

  (get-name [this]
    (::name this))

  (dependencies [this]
    (::depends-on this)))

(def example-component
  {::depends-on [:sa]
   ::name :server
   ::started? true
   ::start-fn (fn [this context] (println (str "<<<<" this)) this)
   ::stop-fn (fn [this] (println "stoping") this)})

(def example-component2
  {::name :sa
   ::started? true
   ::start-fn (fn [this context] (println (str "<<sasasasa<<" this)) this)
   ::stop-fn (fn [this] (println "stoping111111") this)})

(def parsed-system
  {:components {:sa example-component2
                :server example-component}})
(def example-system
  {:components [example-component2 example-component]})

;; Default system atom
(def system (atom {}))



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


;; DONE ---------------------------------------------
(defn get-dependencies-of
  [system-map component]
  (let [dependencies (dependencies component)]
    (filter #(some #{(get-name %)} dependencies)
            (vals (get-components system-map)))))

(get-dependencies-of parsed-system example-component)

;; DONE ----------------------------------------------
(defn start-component!
  "Starts the given `component` of the given `system`."
  [system-map component]
  (if (started? component)
    system-map
    (update-in (reduce start-component! system-map
                       (get-dependencies-of system-map component))
               [:components (get-name component)]
               (fn [_] (start! component {})))))


(start-component! parsed-system example-component)

;; DONE ----------------
(defn stop-component!
  [system-map component]
  (if-not (started? component)
    system-map
    (reduce stop-component!
            (update-in system-map
                       [:components (get-name component)]
                       (fn [_] (stop! component)))
            (get-dependencies-of system-map component))))


(stop-component! parsed-system example-component)

;; DONE ----------------
(s/def ::system-map (s/and map?
                           #(contains? % :components)
                           #(map? (:components %))))


(defn start-system!
  "Starts the given `system-map`."
  [system-map]
  (if-not (s/valid? ::system-map system-map)
    (throw (ex-info "Provided system is not valid" {:cause (s/explain ::system-map system-map)}))
    (reduce start-component! system-map (vals (get-components system-map)))))

(start-system! parsed-system)
;; ----------------------

;; DONE ----------------------
(defn stop-system!
  [system-map]
  (reduce stop-component! system-map (vals (get-components system-map))))

;; ------------------------




(defn update-system-components!
  [components]
  (let [new-system (update-in @system key)])
  (reset system [key value]
           (swap! system #(update-in % key value))))


(set-system! example-system)



(extend-protocol ISystem
  clojure.lang.PersistentArrayMap
  (components [this]
    (get-components this))

  (get-component [this component-name]'
    (component-name (components this)))

  clojure.lang.Atom
  (components [this]
    (get-components @this))
  (get-component [this component-name]
    (component-name (components @this))))








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
    (updat! system
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
