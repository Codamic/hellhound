(ns hellhound.streams
  (:require
   [manifold.stream                :as s]
   [hellhound.utils                :refer [todo]]
   [hellhound.streams.impl.channel]))

;; IMPORTANT NOTE: At this point, this namespace is just a proxy for
;; manifold.stream. But in near future we want to introduce our own
;; stream implementation which will work with manifold as well but
;; with completely different implementation. The goals for the new
;; implementation is to provide necessary means for us to have better
;; control over concurrency and parallelism and also better abstraction
;; that helps us to distribute the components load in a cluster.
;;
;; So for now these functions are just symlinks for the manifold
;; streams functions.
(def stream  s/stream)
(def stream? s/stream?)

(def consume s/consume)
(def put! s/put!)
(def try-put! s/try-put!)

(def take! s/take!)
(def try-take! s/try-take!)

(def connect s/connect)
(def connect-via s/connect-via)

(def map s/map)
(def transform s/transform)

(def description s/description)
(def downstream s/downstream)

(def close! s/close!)
;; ----------------------------------------------------------------------------
