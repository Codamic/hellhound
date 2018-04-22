(ns hellhound.system.execution
  (:require [manifold.stream :as s]
            [manifold.deferred :as d]
            [manifold.executor :as e]))

(defn get-executor
  [system]
  (:executor system))

(defn get-execution-model
  [system]
  (:execution-model system))


(def s1 (s/stream 10))
(def s2 (s/stream 10))
(def s3 (s/stream 10))

(def tn #(.getName (Thread/currentThread)))
(def e1 (e/fixed-thread-executor 2))
(def e2 (e/fixed-thread-executor 2))

(s/onto e1 s1)
(s/onto e2 s2)

(s/put! s1 (tn))
(s/consume (fn [x]
             (println "s1:")
             (println (tn))
             (println x)
             (s/put! s2 x)) s1)

(s/consume (fn [x]
             (println "s2: -------------")
             (println (tn))
             (println x)) s2)
