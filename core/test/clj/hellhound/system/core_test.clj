(ns hellhound.system.core_test
  (:require
   [clojure.test :refer [deftest run-tests]]
   [clojure.spec.test.alpha :as stest]
   [hellhound.component     :as hcomp]
   [hellhound.system.core   :as sut]
   [hellhound.test          :as ht]))



(def subject-system
  {:components [(hcomp/make-component ::c1
                                      (fn [this ctx ] this)
                                      #(identity %))
                (hcomp/make-component ::c2
                                      (fn [this ctx ] this)
                                      #(identity %)
                                      [::c1])]})

(println "xxx")
(deftest context-for
  (let [ctx-map (sut/context-for subject-system
                                 (second (:components subject-system)))]
    (println "sadasdads")
    (is (map? ctx-map))))

(deftest spec-test
  (ht/ns-spec-tests 'hellhound.system.core))

(run-tests)
