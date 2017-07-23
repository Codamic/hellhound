(ns hellhound.http.websocket.json
  (:require
   [cheshire.core                 :as json]
   [hellhound.http.websocket.core :as core]))

(deftype JsonPacker []
  core/IPacker
  (pack   [this data & options]
    (json/generate-string data))
  (unpack [this data & options]
    (json/parse-string data)))
