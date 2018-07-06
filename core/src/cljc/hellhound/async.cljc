(ns hellhound.async
  "TODO: Write docstring for this ns.
   CAUTION: Experimental ns."
  {:added 1.0
   :experimental true}
  (:refer-clojure :exclude [future zip realized?])
  (:require
   [manifold.deferred :as d]
   [hellhound.system :as hellhound]
   [hellhound.system.execution :as exec]))

;; IMPORTANT NOTE: At this point, this namespace is just a proxy for
;; manifold.deferred. But in near future we want to introduce our own
;; deferred implementation which will work with manifold as well but
;; with completely different implementation. The goals for the new
;; implementation is to provide necessary means for us to have better
;; control over concurrency and parallelism and also better abstraction
;; that helps us to distribute the components load in a cluster.
;;
;; So for now these functions are just symlinks for the manifold
;; streams functions.
(def alt d/alt)

(def deferred d/deferred)
(def deferred? d/deferred?)
(def deferrable? d/deferrable?)

(def catch d/catch)
(def finally d/finally)

(defmacro future
  [& body]
  `(manifold.deferred/future ~@body))

(defmacro future-with
  [& body]
  `(manifold.deferred/future-with ~@body))

(def chain d/chain)
(def onto d/onto)

(def realized? d/realized?)

(def error! d/error!)
(def error-deferred d/error-deferred)

(def success! d/success!)
(def success-deferred d/success-deferred)

(def timeout! d/timeout!)

(def zip d/zip)

(defn execute-io!
  [f]
  ;;   (exec/execute-io-with-system  f)
  (d/future-with (exec/wait-pool (hellhound/system))
                 (f)))

(defn execute
  [f]
  ;;(exec/execute-with-system (hellhound/system) f)
  (d/future-with (exec/execution-pool (hellhound/system))
                 (f)))
