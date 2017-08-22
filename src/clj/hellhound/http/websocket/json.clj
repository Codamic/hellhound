(ns hellhound.http.websocket.json
  "Pretty simple `IPacker` implementation for `JSON` data."
  (:require
   [cheshire.core                 :as json]
   [hellhound.http.websocket.core :as core]))

(defrecord JsonPacker []
  core/IPacker
  (pack   [this data & options]
    (json/generate-string data))
  (unpack [this data & options]
    (json/parse-string data)))


(def json-packer (->JsonPacker))

(defn pack
  [data & options]
  (apply core/pack json-packer data options))

(defn unpack
  [data & options]
  (apply core/unpack json-packer data options))
