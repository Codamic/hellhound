(ns hellhound.streams.protocols)

(defprotocol Consumable
  ;; consume should be async. It might block if buffer is empty
  ;; and there is no pending put operation.
  (consume [stream f]
    "Asynchronously consumes values from `stream` and applies `f` on each value."))

(defprotocol Sinkable
  ;; put should be async. It might block if buffer is full and
  ;; there is no consume operation.
  (put! [stream v]
    "Asynchronously puts `v` on the stream."))

(defprotocol Connectable
  (connect [sink source] "Connects `sink` to `source`")
  (connect-via [sink source f] "Connects `sink` to `source` by applying `f` to values."))

(defprotocol Closable
  (close! [stream] "Closes the stream."))
