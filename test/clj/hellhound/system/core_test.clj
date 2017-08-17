(ns hellhound.system.core_test
  (:require [hellhound.system.core :as sut]
            [clojure.test :refer [deftest]]
            [hellhound.test :as ht]
            [clojure.spec.test.alpha :as stest]))


(deftest spec-test
  (ht/ns-spec-tests 'hellhound.system.core))
