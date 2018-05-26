(ns hellhound.system-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t :refer [deftest testing is are]]
            [hellhound.component :as hcomp]
            [hellhound.system  :as system]
            [hellhound.streams :as stream]))


;; System tests --------------------------------------------
(def component-counter (atom 0))

(defn sample-start-fn
  [key value]
  (fn [component context]
    (reset! component-counter (inc @component-counter))
    (let [input  (hcomp/input component)
          output (hcomp/output component)]
      (stream/connect input output)
      (assoc component
             key value
             :context context
             :counter @component-counter))))

(defn sample-stop-fn
  [key]
  (fn [component]
    (dissoc (assoc component :stopped? true) key)))

(def sample-system
  {:components
   [(hcomp/make-component :sample/component2
      (sample-start-fn :key2 :value2)
      (sample-stop-fn :key2)
      [:sample/component1])
    (hcomp/make-component :sample/component1
      (sample-start-fn :key1 :value1)
      (sample-stop-fn :key1))
    (hcomp/make-component :sample/component3
      (sample-start-fn :key3 :value3)
      (sample-stop-fn :key3)
      [:sample/component1])]

   ;; The order is intentionally reverse just for testing
   :workflow [[:sample/component1 :sample/component2]
              [:sample/component2 :sample/component3]]})


(deftest system-test
  (testing "Simple working system"
    (system/set-system! sample-system)
    (system/start!)

    (let [subject     (system/system)
          component1  (system/get-component :sample/component1)
          component2  (system/get-component :sample/component2)
          component3  (system/get-component :sample/component3)]

      (testing "Testimg system/get-component"
        (is (not (nil? component1))))


      (testing "Testing start-fn of component"
        (is (= :value1 (:key1 component1)))
        (is (hcomp/started? component1))
        (is (hcomp/started? component2))
        (is (hcomp/started? component3)))

      (testing "Testing dependency of components"
        (is (< (:counter component1) (:counter component2)))
        (let [ctx2 (:context component2)
              deps2 (:dependencies ctx2)
              deps-map2 (:dependencies-map ctx2)]
          (is (hcomp/started? (first deps2)))
          (is (= component1 (first deps2)))
          (is (= component1 (:sample/component1 deps-map2)))))

      (testing "Workflow"
        (let [input1  (hcomp/input component1)
              input2  (hcomp/input component2)
              input3  (hcomp/input component3)
              output1 (hcomp/output component1)
              output2 (hcomp/output component2)
              output3 (hcomp/output component3)]

          (is (not (nil? input1)))
          (is (not (nil? input2)))
          (is (not (nil? output1)))
          (is (not (nil? output2)))

          (when (and input1 input2 output1 output2)
            (is (stream/stream? input1))
            (is (stream/stream? input2))
            (is (stream/stream? output1))
            (is (stream/stream? output2))

            ;; (is (= (second (first (stream/downstream input1))) output1))
            ;; (is (= (second (first (stream/downstream input2))) output2))
            ;; (is (= (second (first (stream/downstream input3))) output3))
            ;; (is (= (second (first (stream/downstream output1))) input2))
            ;; (is (= (second (first (stream/downstream output2))) input3))
            (is (true? (stream/try-put! input1 10 1000)))))))


    (system/stop!)))

(t/run-tests)
