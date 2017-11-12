(ns aug
  (:require [clojure.spec.alpha :as s]
            [manifold.stream :as stream]
            [clojure.core.async :as async]
            [clojure.spec.test.alpha :as stest]))


(def input-stream (stream/stream))

(def a (async/chan 10))
(def b (async/chan 10))
(def c (async/chan 3))
(def d (async/chan 10))
(def z (async/chan 10))


(stream/connect c input-stream)
(stream/connect a input-stream)
(stream/connect input-stream b)
(stream/connect input-stream d)

(async/>!! a 12)
(async/>!! z 12000)
(async/close! c)
(async/close! a)
(stream/connect z input-stream)

(async/<!! b)
(async/<!! d)

(defn f1
  [a b]
  (* a b))

(s/fdef f1
        :args (s/cat :a int? :b int?)
        :ret int?
        :fn #(= (/ (:ret %) (:a (:args %)))
                (:b (:args %))))


(s/exercise `f1)


(s/def ::integer (s/and int? pos?))
(s/def ::yechi (s/coll-of ::integer :kind vector?))
(s/exercise ::yechi)
