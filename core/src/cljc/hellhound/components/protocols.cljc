(ns hellhound.components.protocols)


(defprotocol Splitter
  (connect [this sink operation-map]
    "Setup the `sink` channel to be connected to a source later based on
     the given `operation-map` which basically defines all the operations
     that should apply to the value before sending it to the sink. Operations
     like filter and map.")

  (commit [_] "Connect source channel to all the sinks"))

(def default-operations
  {:filter-fn #(identity %)
   :map-fn    #(identity %)})

(defn ->sink
  "Send the given `value` to the given `sink` channel by applying changes
  defined in `op-map` to the value. "
  [sink value {:keys [filter-fn map-fn :as ops]}]
  (if (filter-fn value)
    (>! sink (map-fn value))))

(defn send->sink
  "Send the given `value` to all the `sinks` by applying changes defined
  in `op-map` to the value. If the value is `nil` it will close all the
  `sinks`."
  [sinks value]
  (if value
    (doseq [[sink op-map] sinks]
      (->sink sink op-map value))
    (doseq [[sink _] sinks]
      (close! sink))))

(deftype OutputSplitter [^Channel source sinks]
  Splitter
  (connect
    [this sink operation-map]
    (let [m (or operation-map default-operations)]
      (conj sinks [sink m])))

  (commit
    [this]
    (go-loop []
      (let [v (<! source)]
        (send->sink sinks value)
        (recur)))))
