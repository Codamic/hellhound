(ns hellhound.streams
  "TODO: Write docstring for this ns.
   CAUTION: Experimental ns."
  (:refer-clojure :exclude [map])
  (:require
   [manifold.stream :as s]
   [hellhound.system.async :as async]
   [hellhound.utils :refer [todo]]))


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
(def closed? s/closed?)


;; ----------------------------------------------------------------------------
(defn >>
  ([s v]
   (>> s v identity))
  ([s v f]
   (async/execute
    (fn []
      ;; TODO: We need to handle the situation which the
      ;; stream is closed.
      (println "xxxxxxxxxxxxxxxxxxxx")
      (println s)
      (println v)
      (f @(put! s v))))))

;; TODO: Is there any scenario for running a blocking code on the
;; put call back ?
(defn >>!
  ([s v]
   (>>! s v identity))
  ([s v f]
   (async/execute-io!
    (fn []
      ;; TODO: We need to handle the situation which the
      ;; stream is closed.
      (f @(put! s v))))))

(defn <<
  [s f]
  (async/execute
   (fn []
     (f @(take! s)))))

(defn <<!
  [s f]
  (async/execute-io!
   (fn []
     (f @(take! s)))))
