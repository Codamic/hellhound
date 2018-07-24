(ns hellhound.streams.protocols)


(defprotocol Consumable
  ;; consume should be async. It might block if buffer is empty
  ;; and there is no pending put operation.
  (consume [stream f]
    "Asynchronously consumes values from `stream` and applies `f` on each value.")
  (take! [stream]
    "Asynchronously takes a value from the given `stream`.")
  (try-take! [stream timeout]
    "Try to asynchronously take a value from the given `stream` in the given `timeout`."))


(defprotocol Sinkable
  ;; put should be async. It might block if buffer is full and
  ;; there is no consume operation.
  (put!
    [stream v]
    [stream v f]
    "Asynchronously puts `v` on the stream.")
  (try-put!
    [stream v]
    [stream v timeout]
    "Try to put the value `v` into the given `stream` with the given `timeout`.
     Returns a boolean value."))


(defprotocol Connectable
  (connect [sink source] "Connects `sink` to `source`")
  (connect-via [sink source f] "Connects `sink` to `source` by applying `f` to values."))


(defprotocol Closable
  (close! [stream] "Closes the stream."))
