(ns hellhound.system.core
  (:require [hellhound.system.core :as sut]
            [clojure.test :as t]
            [clojure.spec.test.alpha :as stest]))


(t/testing "System Core specs"
  (-> (stest/enumerate-namespace 'hellhound.system.core)
      (stest/check)))
