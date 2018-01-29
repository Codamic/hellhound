(ns hellhound.http.websocket.json
  "Pretty simple `IPacker` implementation for `JSON` data."
  (:require
   [cheshire.core                 :as json]
   [hellhound.http.websocket.core :as core]))

(defrecord JsonPacker []
  core/IPacker
  (pack   [this data options]
    (json/generate-string data))
  (unpack [this data options]
    (json/parse-string data)))


(def json-packer (->JsonPacker))

(defn pack
  [data]
  (apply core/pack json-packer data {}))

(defn unpack
  [data]
  (core/unpack json-packer data nil))
