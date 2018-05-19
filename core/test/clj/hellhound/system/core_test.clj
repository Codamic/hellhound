(ns hellhound.system.core_test
  (:require
   [clojure.test :refer [deftest run-tests is]]
   [clojure.spec.test.alpha :as stest]
   [hellhound.component     :as hcomp]
   [hellhound.system.core   :as sut]
   [hellhound.test          :as ht]))


(def c1 (hcomp/make-component ::c1
                              (fn [this ctx ] this)
                              #(identity %)))
(def c2 (hcomp/make-component ::c2
                              (fn [this ctx ] this)
                              #(identity %)
                              [::c1]))
(def subject-system
  {:components [c1 c2]})



(deftest context-for
  (println "sadasdads")
  (println c2)
  (let [ctx-map (sut/context-for subject-system c2)]
    (is (map? ctx-map))))

(deftest spec-test
  (ht/ns-spec-tests 'hellhound.system.core))

(run-tests)
