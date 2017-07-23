(ns hellhound.http.websocket.core)


(defprotocol IPacker
  (pack   [this data & options])
  (unpack [this data & options]))
