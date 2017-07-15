(ns hellhound.components.core
  "A very light weight and effecient implementation of clojure components."
  (:require
   [clojure.spec.alpha             :as spec]
   [hellhound.core                 :as hellhound]
   [hellhound.system.core          :as system]
   [hellhound.components.protocols :as protocols]))

;; Private Functions -----------------------------
(declare start-component)

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
  (let [sys (update-in system [:components name :instance] (fn [_] component))]
    (update-in sys [:components name :started?] (fn [_] true))))

(defn- update-stopped-system
  "Update the system with the given sopped component data"
  [system name component]
  (let [sys (update-in system [:components name :instance] (fn [_] component))]
    (update-in sys [:components name :started?] (fn [_] false))))


(defn- extract-output-of
  [component-name]
  (let [component (system/get-system-entry component-name)]

    (if-not (started? component)
      (throw-exception "Unit '%s' is not started yet." component-name))

    (let [input (:output (:instance component))]
      (if (nil? input)
        (throw-exception "Unit '%s' does not provide an :output."
                         component-name))
      input)))

(defn- gather-inputs
  "Gather all the inputs for the given component name from the provided list of
  input components"
  [name]
  (let [input-names (:inputs (system/get-system-entry name))]
    (if (and (not (nil? input-names))
             (vector? input-names))
      (map  extract-output-of input-names)
      [])))

(defn- start-dependencies
  [{:keys [name data system] :as all}]
  (let [requirements (or (:requires data) [])
        record       (:instance   data)]

    (if-not (empty? requirements)
      ;; In case of any requirement we need to start them first
      (doseq [req-name requirements]
        (start-component req-name (system/get-system-entry req-name) system)))
    all))

(defn- inject-inputs
  [{:keys [name data system] :as all}]
  (let [inputs (gather-inputs name)]
    (if-not (empty? inputs)
      (let [new-record   (assoc (:instance data) :inputs inputs)
            new-data     (update-in data [:instance] (fn [_] new-record))]
        {:name name :data new-data :system system})
      all)))

(defn- run-the-start-method
  [{:keys [name data system] :as all}]
  (let [record            (:instance data)]

    ;; I required component was not defined in the system
    (if (nil? record)
      (throw (ex-info (format "Can't find '%s' component in the system" name) {})))

    ;; Replace the record value with the started instance
    ;;(swap! system update-started-system name (.start record))
    (system/update-system!
     (fn [system] (update-started-system system name (.start record))))
    (assoc all :system system)))

;; Public Functions ------------------------------
(defn start-component
  "Start the component given by `name` from the default system map or
  the given `system`."
  ([name]
   (let [component (system/get-system-entry name)]
     (start-component name component)))

  ([name data]
   (start-component name data (system/get-system)))

  ([name data system]
   (let [bundle {:name name :data data :system system}]
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
   (let [component (system/get-system-entry name)]
     (stop-component name component)))

  ([name data]
   (stop-component name data (system/get-system)))

  ([name data system]
   (if (started? data)
     (let [requirements (or (:requires data) [])
           record       (:instance   data)]
       (let [stopped-component (.stop record)]
         ;;(swap! system update-stopped-system name stopped-component))
         (system/update-system!
          (fn [system] (update-stopped-system system name stopped-component))))

       (if-not (empty? requirements)
         (doseq [req-name requirements]
           (stop-component req-name (get (:components system) req-name) system)))))))

(defn iterate-components
  "Iterate over system components"
  [system f]
  (let [components (:components system)]
    (doseq [[component-name component-data] components]
      (if (satisfies? protocols/Lifecycle (:instance component-data))
        (f component-name component-data system)
        (throw (Exception. (format "'%s' component does not satisfy the 'Lifecycle' protocol."
                                   component-name)))))))
