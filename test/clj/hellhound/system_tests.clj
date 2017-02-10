(ns hellhound.system-tests
  (:require [hellhound.system :as sut]
            [clojure.test     :refer :all]))


(defn test-component
  [system]
  (assoc-in system [:test-component] (fn [] nil)))

(sut/defsystem test-system
  (test-component))

(deftest defsystem-test
  (let [subject test-system]
    (is (function? subject))
    (is (function? (:test-component (subject))))))
