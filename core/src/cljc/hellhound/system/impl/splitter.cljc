(ns hellhound.system.impl.splitter
  {:added 1.0}
  (:require
   [hellhound.system.operations :as op]
   [hellhound.streams :as streams]
   [hellhound.system.protocols :as proto]
   [hellhound.utils :refer [todo]]))


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
                           (streams/put! sink tv)))
                       sink))


(deftype OutputSplitter [source sinks]
  proto/Splitter
  (connect
    [this sink operation-map]
    ;; Simply puts the sink and it's operation-map into sinks vector
    (let [m (or operation-map op/default-operations)]
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
