(ns hellhound.system.impl.output-splitter
  (:require
   [clojure.core.async.impl.channels :as channels]
   [clojure.core.async :as async :refer [go <! >! chan close! go-loop]]
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
  [put sink value {:keys [filter-fn map-fn] :as ops}]
  (if (filter-fn value)
    (put sink (map-fn value))))

(defn send->sink
  "Send the given `value` to all the `sinks` by applying changes defined
  in `op-map` to the value. If the value is `nil` it will close all the
  `sinks`."
  [put sinks value]
  (if value
    (doseq [[sink op-map] sinks]
      (println "SINK: " sink)
      (->sink put sink value op-map))
    (doseq [[sink _] sinks]
      (close! sink))))

(deftype OutputSplitter [source sinks]
  proto/Splitter
  (connect
    [this sink operation-map]
    (let [m (or operation-map default-operations)]
      (swap! sinks conj [sink m])))

  (commit
    [this]
    (go-loop []
      (let [v (<! source)]
        (println "xxxeeeee")
        (println >!)
        (send->sink #(>! %1 %2) @sinks v)
        (recur)))))


(defn output-splitter
  [source]
  (OutputSplitter. source (atom [])))

(comment
  (let [a (chan 10)
        b (chan 10)
        c (chan 10)
        splitter (output-splitter a)
        read #(async/go-loop []
                (let [v (async/<! %1)]
                  (println (format "GO-%s: %s" %2 v)))
                (recur))]
    (read b "b")
    (read c "c")
    (proto/connect splitter b default-operations)
    (proto/connect splitter c default-operations)
    (proto/commit splitter)
    (println "xxxx")
    (println (.sinks splitter))
    (println c)
    (println default-operations)
    (doseq [x [1 2 3 4 5 6 7 8 9 10 11 12 13]]
          (async/>!! a x))))
