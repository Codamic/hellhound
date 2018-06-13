(ns hellhound.streams
  (:require
   [manifold.stream                :as s]
   [hellhound.utils                :refer [todo]]
   [hellhound.streams.impl.channel]))


(def stream  s/stream)
(def stream? s/stream?)

(def consume s/consume)
(def put! s/put!)
(def try-put! s/try-put!)

(def take! s/take!)
(def try-take! s/try-take!)

(def connect s/connect)
(def connect-via s/connect-via)
