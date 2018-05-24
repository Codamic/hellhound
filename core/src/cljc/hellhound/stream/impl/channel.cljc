(ns hellhound.stream.impl.channel
  (:require
   [clojure.core.async               :as async]
   [clojure.core.async.impl.channels :as chans]
   [hellhound.stream.protocols       :as impl])
  (:import
   [clojure.core.async.impl.channels ManyToManyChannel]))

(extend-type ManyToManyChannel
  impl/Consumable
  (consume [source f]
    (async/go-loop []
      (let [v (async/<! source)]
        ;; A nil value for `v` indicates that source is closed.
        (when v
          (f v)
          ;; Only process to the next cycle if `v` is not nil. Meaning
          ;; that source is still open.
          (recur)))))

  impl/Sinkable
  (put [sink v]
    (when v
      (async/go (async/>! sink v))))

  impl/Connectable
  (connect [sink source]
    (async/go-loop []
      (let [v (async/<! sink)]
        ;; A nil value for `v` indicates that source is closed.
        (when v
          (async/>! source v)
          ;; Only process to the next cycle if `v` is not nil. Meaning
          ;; that sink is still open.
          (recur)))))

  ;; TODO: Simplify this block
  (connect-via [sink source f]
    (async/go-loop []
      (when-let [v (async/<! sink)]
        (when-let [output (f v)]
          (async/>! source output))
        (recur)))))

(comment
  (let [c (async/chan 10)]
    (impl/consume c #(println (str "<<<< " %)))
    (impl/put c "hellhound")
    (impl/put c "IO")
    (async/close! c)
    ))
