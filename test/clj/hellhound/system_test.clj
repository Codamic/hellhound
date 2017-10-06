(ns hellhound.system-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t :refer [deftest testing is are]]
            [hellhound.component :as hcomp]
            [hellhound.system :as system :refer [defcomponent]]
            [manifold.stream :as stream]))


;; defcomponent test ---------------------------------------
(def simple-map {:hellhound.component/name       :component/name
                 :hellhound.component/start-fn   inc
                 :hellhound.component/stop-fn    inc
                 :hellhound.component/depends-on []})

(deftest defcomponent-test
  (testing "defcomponent"
    (is (= simple-map (defcomponent :component/name inc inc)))
    (is (= simple-map (defcomponent :component/name inc inc [])))))

;; System tests --------------------------------------------
(def component-counter (atom 0))

(defn sample-start-fn
  [key value]
  (fn [component context]
    (reset! component-counter (inc @component-counter))
    (let [input  (hcomp/input component)
          output (hcomp/output component)]
      (assoc component
             key value
             :counter @component-counter))))


(defn sample-stop-fn
  [key]
  (fn [component]
    (dissoc (assoc component :stopped? true) key)))

(def sample-system
  {:components
   [(defcomponent :sample/component1
      (sample-start-fn :key1 :value1)
      (sample-stop-fn :key1)
      [:sample/component2])
    (defcomponent :sample/component2
      (sample-start-fn :key2 :value2)
      (sample-stop-fn :key2))]
   ;; The order is intentionally reverse just for testing
   :workflow [[:sample/component1 :sample/component2]]})


(deftest system-test
  (testing "Simple working system"
    (system/set-system! sample-system)
    (system/start!)

    (let [subject     (system/system)
          component1  (system/get-component :sample/component1)
          component2  (system/get-component :sample/component2)]

      (testing "Testimg system/get-component"
        (is (not (nil? component1))))

      (testing "Testing start-fn of component"
        (is (= :value1 (:key1 component1)))
        (is (:hellhound.component/started? component1))
        (is (:hellhound.component/started? component2)))

      (testing "Testing dependency of components"
        (is (< (:counter component1) (:counter component2))))

      (testing "Workflow"
        (let [input1  (hcomp/input component1)
              input2  (hcomp/input component2)
              output1 (hcomp/output component2)
              output2 (hcomp/output component2)]

          (is (not (nil? input1)))
          (is (not (nil? input2)))
          (is (not (nil? output1)))
          (is (not (nil? output2)))

          (when (and input1 input2 output1 output2)
            (is (stream/stream? input1))
            (is (stream/stream? input2))
            (is (stream/stream? output1))
            (is (stream/stream? output2))

            (is (true? @(stream/put! input1 :something)))
            (is (= :something @(stream/try-take! output1
                                                 :nothing
                                                 1000
                                                 :timeout)))))))


    (system/stop!)))


;;(t/run-tests)
