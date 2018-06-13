(ns hellhound.system.impl.splitter
  (:require
   [hellhound.streams :as streams]
   [hellhound.system.protocols :as proto]
   [hellhound.utils :refer [todo]]))

(def
  ^{:doc "A map which describes a set of operations that should apply to values
          from a source channel before putting them on any sink channel."}

  default-operations
  {;; If the function returns a treuthy by passing the value from the source,
   ;; we will the the value to the sink assigned to this map.
   :filter-fn #(identity %)
   ;; This function will apply to the value came from the source channel before
   ;; sending it to the sink channel.
   :map-fn    #(identity %)})

(defn make-ops-map
  [filter-fn map-fn]
  {:filter-fn (or filter-fn #(identity %))
   :map-fn    (or map-fn    #(identity %))})

(defn- transform-and-put
  "Applies all the operations defined in the given `ops` map and returns
  the transformed value."
  [value {:keys [filter-fn map-fn] :as ops}]
  (if (filter-fn value)
    (map-fn value)
    nil))

(defn- connect
  [source sink op-map]
  (streams/connect-via source
                       (fn [v]
                         (when-let [tv (transform-and-put v op-map)]
                           (todo "Should we block here ???")
                           (streams/put! sink tv)))
                       sink))

(deftype OutputSplitter [source sinks]
  proto/Splitter
  (connect
    [this sink operation-map]
    ;; Simply puts the sink and it's operation-map into sinks vector
    (let [m (or operation-map default-operations)]
      (swap! sinks conj [sink m])))

  (commit
    [this]
    (doseq [[sink op-map] @sinks]
      (connect source sink op-map))
    this))


(defn output-splitter
  "Create and return an output-splitter for the given `source` channel."
  [source]
  (OutputSplitter. source (atom [])))

(comment
  (let [a (streams/stream 10)
        b (streams/stream 10)
        c (streams/stream 10)
        splitter (output-splitter a)
        read (fn [x y]
               (streams/consume
                (fn [v]
                  (Thread/sleep 100)
                  (println (format "S-%s: %s" y v))) x))]

    (read b "b")
    (read c "c")
    (proto/connect splitter b default-operations)
    (proto/connect splitter c default-operations)
    (proto/commit splitter)

    (doseq [x [1 2 3 4 5 6 7 8 9 10 11 12 13]]
          (streams/put! a x))))
