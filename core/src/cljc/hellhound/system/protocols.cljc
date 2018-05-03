(ns hellhound.system.protocols)


(defprotocol Splitter
  (connect [this sink operation-map]
    "Setup the `sink` channel to be connected to a source later based on
     the given `operation-map` which basically defines all the operations
     that should apply to the value before sending it to the sink. Operations
     like filter and map.")

  (commit [_] "Connect source channel to all the sinks"))
