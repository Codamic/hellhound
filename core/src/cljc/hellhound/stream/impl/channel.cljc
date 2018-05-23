(ns hellhound.stream.impl.channel
  (:require
   [clojure.core.async               :as async]
   [clojure.core.async.impl.channels :as chans]
   [hellhound.stream.protocols       :as impl]))

(extend-type chans/ManyToManyChannel
  impl/Consumable
  (consume [source f]
    (async/go-loop []
      (let [v (async/<! source)]
        (f v)
        (recur))))

  impl/Sinkable
  (put [sink v]
    (async/go (async/>! sink v)))

  impl/Connectable
  (connect [sink source]
    (async/go-loop []
      (let [v (async/<! sink)]
        (when v
          (async/>! source v)
          (recur)))))

  ;; TODO: Simplify this block
  (connect-via [sink source f]
    (async/go-loop []
      (let [v     (async/<! sink)]
        (when v
          (let [output (f v)]
            (when output
              (async/>! source output)))
          (recur))))))
