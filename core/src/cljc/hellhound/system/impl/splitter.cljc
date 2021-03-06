(ns hellhound.system.impl.splitter
  {:added 1.0}
  (:require
   [hellhound.streams :as streams]
   [hellhound.system.protocols :as proto]
   [hellhound.async :as async]))


(def ^:private default-node
  ;; TODO: Remove this binding and refactor the connect function
  ;;       To not use it
  {:hellhound.workflow/filter   #(identity %)
   :hellhound.workflow/map      #(identity %)})


(defn- transform-and-put
  "Applies all the operations defined in the given `node` map on the given
   `value` and returns the transformed result."
  [value node]
  ;; TODO: Create a protocol for extracting data from Node.
  ;;       I don't like to extract data using keys. this
  ;;       function knows too much.
  (let [filter-fn (or (:hellhound.workflow/filter node)
                      #(identity %))
        map-fn (or (:hellhound.workflow/map node)
                   #(identity %))]
    (when (filter-fn value)
      (map-fn value))))


(defn- connect
  [source sink node]
  (streams/connect-via source
                       (fn [v]
                         (when-let [tv (transform-and-put v node)]
                           (streams/put! sink tv))

                         ;; Quick hack to prevent the downstream to get closed
                         (async/success-deferred true))
                       sink))


(deftype OutputSplitter [source sinks]
  proto/Splitter
  (connect
    [this sink node]
    ;; Simply puts the sink and it's operation-map into sinks vector
    (let [m (or node default-node)]
      (swap! sinks conj [sink m])))

  (commit
    [this]
    (doseq [[sink op-map] @sinks]
      (connect source sink op-map))
    this)

  (close!
    [this]
    (reset! sinks [])
    this))


(defn output-splitter
  "Create and return an output-splitter for the given `source` channel."
  [source]
  (OutputSplitter. source (atom [])))
