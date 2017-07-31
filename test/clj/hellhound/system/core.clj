(ns hellhound.system.core
  (:require [hellhound.system.core :as sut]
            [clojure.test :as t]
            [clojure.spec.test.alpha :as stest]))


(t/testing "Core specs"
  (stest/check `hellhound.system.core/start-system))
