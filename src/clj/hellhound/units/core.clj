(ns hellhound.units.core
  "Unit is the same concept as components")

;; Main storage for system data.
(def ^:private default-system (atom {}))

;; Protocols --------------------------------------
(defprotocol Structure
  "This protocol defines the structure of each unit"
  (start [unit])
  (stop  [unit]))

;; Private Functions -------------------------------
(declare get-system-entry start-unit)

(defn- throw-exception
  [& rest]
  (throw (Exception. (apply format rest))))

(defn- started?
  "Returns true if unit already started"
  [unit-data]
  (:started? unit-data))

(defn- update-started-system
  "Update the system with the given started unit data"
  [system name unit]
  (let [sys (update-in system [:units name :record] (fn [_] unit))]
    (update-in sys [:units name :started?] (fn [_] true))))

(defn- update-stopped-system
  "Update the system with the given sopped unit data"
  [system name unit]
  (let [sys (update-in system [:units name :record] (fn [_] unit))]
    (update-in sys [:units name :started?] (fn [_] false))))


(defn- extract-output-of
  [unit-name]
  (let [unit (get-system-entry unit-name)]

    (if-not (started? unit)
      (throw-exception "Unit '%s' is not started yet." unit-name))

    (let [input (:output (:record unit))]
      (if (nil? input)
        (throw-exception "Unit '%s' does not provide an :output."
                         unit-name))
      input)))

(defn- gather-inputs
  "Gather all the inputs for the given unit name from the provided list of
  input units"
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
        (start-unit req-name (get-system-entry req-name) system)))
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
        started-unit (.start record)]
    ;; Replace the record value with the started instance
    (swap! system update-started-system name started-unit)
    (assoc all :system system)))

(defn- start-unit
  "Start the given unit"
  [name data system]
  (let [bundle {:name name :data data :system system}]
    (if-not (started? data)
      (-> bundle
          (start-dependencies)
          (inject-inputs)
          (run-the-start-method))
      bundle)))

(defn- stop-unit
  "Stop the given unit"
  [name data system]
  (if (started? data)
    (let [requirements (or (:requires data) [])
          record       (:record   data)]
      (let [stopped-unit (.stop record)]
        (swap! system update-stopped-system name stopped-unit))

      (if-not (empty? requirements)
        (doseq [req-name requirements]
          (stop-unit req-name (get (:units @system) req-name) system))))))

(defn- iterate-units
  "Iterate over system units"
  [system f]
  (let [units (:units @system)]
    (doseq [[unit-name unit-data] units]
      (if (satisfies? Structure (:record unit-data))
        (do
          (f unit-name unit-data system))
        (throw (Exception. (format "'%s' unit does not satisfy the 'Structure' protocol."
                                   unit-name)))))))

;; Global Functions ---------------------------
(defn set-system!
  "Set the default system"
  [system]
  (swap! default-system (fn [_] system)))

(defn system
  []
  @default-system)

(defn get-system-entry
  [unit-name]
  (get (:units (system)) unit-name))

(defn get-unit
  [unit-name]
  (:record (get-system-entry unit-name)))

(defn start-system
  "Start the given system and call start on all the units"
  [system]
  (iterate-units system start-unit))

(defn stop-system
  "Stop the given system by calling stop on all units"
  [system]
  (iterate-units system stop-unit))

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
