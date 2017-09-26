(ns aug
  (:require [clojure.spec.alpha :as s]
            [manifold.stream :as stream]
            [clojure.spec.test.alpha :as stest]))


(def input-stream (stream/stream))
