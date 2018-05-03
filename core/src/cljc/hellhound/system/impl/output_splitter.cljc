(ns hellhound.system.impl.output-splitter
  (:require
   [clojure.core.async :refer [go <! >!]]
   [hellhound.system.protocols :as proto]))

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
  proto/Splitter
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
