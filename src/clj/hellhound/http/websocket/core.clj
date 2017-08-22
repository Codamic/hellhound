(ns hellhound.http.websocket.core
  "Each packer should implement `IPacker` protocol. For more information and
  an example checkout `hellhound.http.websocket.json` namespace.")

(defprotocol IPacker
  "The abstraction aroud packing and unpacking data on the websocket."
  (pack   [this data & options] "Encodes data to string or binary data.")
  (unpack [this data & options] "Decodes data from string or binary data."))
