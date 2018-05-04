(ns hellhound.system.impl.splitter
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

(defn- transorm-value
  "Applies all the operations defined in the given `ops` map and return the
  transformed value."
  [value {:keys [filter-fn map-fn] :as ops}]
  (if (filter-fn value)
    (map-fn value)
    nil))


(deftype OutputSplitter [source sinks]
  proto/Splitter
  (connect
    [this sink operation-map]
    ;; Simply puts the sink and it's operation-map into sinks vector
    (let [m (or operation-map default-operations)]
      (swap! sinks conj [sink m])))

  (commit
    [this]
    (go-loop []
      ;; Asynchronously reads a value from the source and transform
      ;; it based on operation map of each sink and if it was'nt nil
      ;; put it into sink.
      (let [value (<! source)
            sinks @sinks]

        (if (nil? value)
          ;; Closes all the sinks if the value from source is nil which means
          ;; the source channel is closed.
          (doseq [[sink _] sinks]
            (close! sink))

          ;; filter and transorm the value according to the operation map
          ;; of each sink and put it on the sink channel.
          (doseq [[sink ops] sinks]
            (let [filtered-value (transorm-value v ops)]
              (when (not (nil? filtered-value))
                (>! sink filtered-value)))))
        (recur)))))


(defn output-splitter
  "Create and return an output-splitter for the given `source` channel."
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
