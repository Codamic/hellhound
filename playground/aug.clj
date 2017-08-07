(ns aug)

(defprotocol ISystem
  (start [this]
    "Starts the system.")

  (stop  [this]
    "stops the system")

  (restart [this]
    "Restarts the system")

  (components [this]
    "Returns a hashmap of all the components defined in the system")

  (get-component [this component-name]
    "Return the instance of the component with the given name")

  (update! [this key value]
    "Updates the system with given value under the given key"))



(defprotocol IComponent
  "This protocol defines a very basic component for hellhound system."
  (start! [component context]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started? [component]
    "Returns a `true` if component started and `false` otherwise.")

  (dependencies [component]
    "Returns a vector of dependency names."))



(extend-protocol IComponent
  clojure.lang.PersistentArrayMap
  (start! [this context]
    (let [start-fn (::start-fn this)]
      (start-fn this context)))

  (stop! [this]
    (let [stop-fn (::stop-fn this)]
      (stop-fn this)))

  (started? [this]
    (or (::started? this) false))

  (dependencies [this]
    (::depends-on this)))

(def example-component
  {::depends-on [:sa]
   ::name :server
   ::started? true
   ::start-fn (fn [this context] (println (str "<<<<" context)))
   ::stop-fn (fn [this] (println "stoping"))})

(def example-system
  {:components [example-component]})



(s/fdef get-components
        :args (s/cat :system map?)
        :ret vector?
        :fn #(= (:ret %) (-> :args :system :components)))

(defn get-components
  "Returns the components catalog of the given `system`."
  [system]
  (:components system))

;; Default system atom
(def system (atom {}))

(defn set-system!
  [system-map]
  (reset! system system-map))

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

  (get-component [this component-name]
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
