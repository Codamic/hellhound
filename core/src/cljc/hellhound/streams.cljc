(ns hellhound.streams
  "TODO: Write docstring for this ns.
   CAUTION: Experimental ns."
  (:refer-clojure :exclude [map reduce])
  (:require
   [manifold.stream :as s]
   [hellhound.system.async :as async]
   [hellhound.async :as d]
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
(def reduce s/reduce)

(defn- handle-execption
  [e]
  (todo "Improve the handle-exception to use supervisor streams")
  (println e))


(defn- callback-wrapper
  [f]
  (try
    (f)
    (catch Exception e
      (handle-execption e)))
  ;; A hack to prevent source stream to get close.
  ;; If the consume callback return falsy deferred
  ;; it means that the sink is closed and source
  ;; should close as well. which we don't want it
  ;; to happen
  (d/success-deferred true))

(defn on-message
  [source f]
  (s/consume f source))

;; TODO: Fix consume! to be run on calculaiton threadpool
(defn consume
  [f source]
  (s/consume (fn [value]
               (callback-wrapper #(f value)))
             source))

;; TODO: Fix consume! to be run on blocking threadpool
(def consume! consume)

(def consume-async s/consume-async)

(defn put!
  [sink value]
  (s/put! sink value))

(def try-put! s/try-put!)

(def take! s/take!)
(def try-take! s/try-take!)

(def connect s/connect)
(defn connect-via
  [source f sink]
  (s/connect-via source
                 (fn [value]
                   (callback-wrapper #(f value)))
                 sink))

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
      (-> (put! s v)
          (d/chain #(f %)))))))

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
      (-> (put! s v)
          (d/chain #(f %)))))))

(defn <<
  [s f]
  (async/execute
   (fn []
     (-> (take! s)
         (d/chain #(f %))))))

(defn <<!
  [s f]
  (async/execute-io!
   (fn []
     (-> (take! s)
         (d/chain #(f %))))))
