(ns hellhound.system.core_test
  (:require [hellhound.system.core :as sut]
            [clojure.test :as t]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [hellhound.test :as ht]
            [clojure.spec.test.alpha :as stest]))

;; (ht/testspec 'hellhound.system.core/get-components)
;; (ht/defspec-test something 'hellhound.system.core/get-components)

;; (defspec-test dsom ['hellhound.system.core/get-components])

;; (t/deftest something
;;   (assert (stest/check 'hellhound.system.core/get-components)))
(t/deftest get-components
  (let [check-results (stest/check `hellhound.system.core/get-components)
        passed       (every? nil? (map :failure check-results))]
    (t/is passed (first check-results))))
