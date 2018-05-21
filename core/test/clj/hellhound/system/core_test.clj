(ns hellhound.system.core_test
  (:require
   [clojure.test :refer [deftest run-tests is testing]]
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
  (testing "Before setting the system"
    (try
      (sut/context-for subject-system c2)
      (catch clojure.lang.ExceptionInfo e
        (is (= (.getMessage e) "Components map is nil. Did you set the system?")))))

  (testing "After setting the system"
    (let [ctx-map (sut/context-for (sut/make-components-index subject-system)
                                   c2)]
      (is (map? ctx-map))

      (sut/set-system! subject-system)
      (is (map? (sut/context-for (sut/get-system) c2))))))


(deftest get-dependencies-of
  (testing "Uninitialized dependency tree"
    (is (= [] (sut/get-dependencies-of subject-system c2))))
  (testing "initialized dependency tree"
    (is (=  1 (count (sut/get-dependencies-of
                      (sut/make-components-index subject-system) c2))))))

(deftest spec-test
  (ht/ns-spec-tests 'hellhound.system.core))

(run-tests)
