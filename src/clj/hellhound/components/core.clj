(ns hellhound.components.core
  "A very light weight and effecient implementation of clojure components."
  (:require [hellhound.core                 :as hellhound]
            [hellhound.components.protocols :as protocols]))

;; Vars ------------------------------------------
;; Default structure for a system map
(def ^:private default-system-structure
  {:components {}})

;; Main storage for system data.
(def ^:private default-system (atom {}))

;; Private Functions -----------------------------
(declare get-system-entry start-component)

(defn- throw-exception
  [& rest]
  (throw (Exception. (apply format rest))))

(defn- started?
  "Returns true if component already started"
  [component-data]
  (:started? component-data))

(defn- update-started-system
  "Update the system with the given started component data"
  [system name component]
  (let [sys (update-in system [:components name :record] (fn [_] component))]
    (update-in sys [:components name :started?] (fn [_] true))))

(defn- update-stopped-system
  "Update the system with the given sopped component data"
  [system name component]
  (let [sys (update-in system [:components name :record] (fn [_] component))]
    (update-in sys [:components name :started?] (fn [_] false))))


(defn- extract-output-of
  [component-name]
  (let [component (get-system-entry component-name)]

    (if-not (started? component)
      (throw-exception "Unit '%s' is not started yet." component-name))

    (let [input (:output (:record component))]
      (if (nil? input)
        (throw-exception "Unit '%s' does not provide an :output."
                         component-name))
      input)))

(defn- gather-inputs
  "Gather all the inputs for the given component name from the provided list of
  input components"
  [name]
  (let [input-names (:inputs (get-system-entry name))]
    (if (and (not (nil? input-names))
             (vector? input-names))
      (map  extract-output-of input-names)
      [])))

(defn- start-dependencies
  [{:keys [name data system] :as all}]
  (let [requirements (or (:requires data) [])
        record       (:record   data)]

    (if-not (empty? requirements)
      ;; In case of any requirement we need to start them first
      (doseq [req-name requirements]
        (start-component req-name (get-system-entry req-name) system)))
    all))

(defn- inject-inputs
  [{:keys [name data system] :as all}]
  (let [inputs (gather-inputs name)]
    (if-not (empty? inputs)
      (let [new-record   (assoc (:record data) :inputs inputs)
            new-data     (update-in data [:record] (fn [_] new-record))]
        {:name name :data new-data :system system})
      all)))

(defn- run-the-start-method
  [{:keys [name data system] :as all}]
  (let [record       (:record data)
        started-component (.start record)]
    ;; Replace the record value with the started instance
    (swap! system update-started-system name started-component)
    (assoc all :system system)))

;; Public Functions ------------------------------
(defn start-component
  "Start the component given by `name` from the default system map or
  the given `system`."
  ([name]
   (let [component (get-component name)]
     (start-component name component)))

  ([name data]
   (start-component name data @default-system))

  ([name data system]
   (let [bundle {:name name :data data :system system :component (:record data)}]
     (if-not (started? data)
       (-> bundle
           (start-dependencies)
           (inject-inputs)
           (run-the-start-method))
       bundle))))

(defn stop-component
  "Stop the given component by `name` from default system or
  given `system` map."
  ([name]
   (let [component (get-component name)]
     (stop-component name component)))

  ([name data]
   (stop-component name data @default-system))

  ([name data system]
   (if (started? data)
     (let [requirements (or (:requires data) [])
           record       (:record   data)]
       (let [stopped-component (.stop record)]
         (swap! system update-stopped-system name stopped-component))

       (if-not (empty? requirements)
         (doseq [req-name requirements]
           (stop-component req-name (get (:components @system) req-name) system)))))))

(defn iterate-components
  "Iterate over system components"
  [system f]
  (let [components (:components @system)]
    (doseq [[component-name component-data] components]
      (if (satisfies? protocols/Lifecycle (:record component-data))
        (f component-name component-data system)
        (throw (Exception. (format "'%s' component does not satisfy the 'Lifecycle' protocol."
                                   component-name)))))))

(defn set-system!
  "Set the default system"
  [system]
  (swap! default-system (fn [_] system)))

(defn system
  []
  @default-system)

(defn get-system-entry
  [component-name]
  (get (:components (system)) component-name))

(defn get-component
  [component-name]
  (:record (get-system-entry component-name)))

(defn start-system
  "Start the given system and call start on all the components"
  [system]
  ;; Read the configuration for the current runtime environment which
  ;; specified by `HH_ENV` environment. Default env is `:development`
  (hellhound/load-runtime-configuration)
  (iterate-components system start-component))

(defn stop-system
  "Stop the given system by calling stop on all components"
  [system]
  (iterate-components system stop-component))

(defn start
  "Start the default system"
  []
  (start-system default-system))

(defn stop
  "Stop the default system"
  []
  (stop-system default-system))

(defn restart
  "Restart the default system"
  []
  (stop)
  (start))

(defmacro defsystem
  "Define a system map according to clojure component defination."
  [system-name & body]
  `(defn ~system-name [] (-> default-system-structure ~@body)))
