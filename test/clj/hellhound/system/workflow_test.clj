(ns hellhound.system.workflow-test
  (:require [hellhound.system.workflow :as sut]
            [clojure.test :as t :refer [deftest testing is are]]
            [hellhound.component :as hcomp]
            [hellhound.system :as system :refer [defcomponent]]
            [manifold.stream :as stream]))

;; System tests --------------------------------------------
(defn sample-start-fn
  [component context]
  (let [input  (hcomp/input component)
        output (hcomp/output component)]
    (stream/connect input output)
    component))

(defn sample-stop-fn
  [component]
  (assoc component :stopped? true))

(def sample-system
  {:components
   [(defcomponent :sample/component2
      sample-start-fn
      sample-stop-fn
      [:sample/component1])
    (defcomponent :sample/component1
      sample-start-fn
      sample-stop-fn)
    (defcomponent :sample/component3
      sample-start-fn
      sample-stop-fn
      [:sample/component1])]

   :workflow [[:sample/component1 odd? :sample/component2]
              [:sample/component1 even? :sample/component3]]})

(deftest system-test
  (testing "Conditional dataflow"
    (system/set-system! sample-system)
    (system/start!)

    (let [subject     (system/system)
          component1  (system/get-component :sample/component1)
          component2  (system/get-component :sample/component2)
          component3  (system/get-component :sample/component3)
          input1  (hcomp/input component1)
          output2 (hcomp/output component2)
          output3 (hcomp/output component3)]

      (is (true? @(stream/put! input1 1)))
      (is (true? @(stream/put! input1 2)))
      (is (true? @(stream/put! input1 3)))
      (is (true? @(stream/put! input1 4)))
      (is (true? @(stream/put! input1 5)))
      (is (true? @(stream/put! input1 6)))
      (is (true? @(stream/put! input1 7)))
      (is (true? @(stream/put! input1 8)))

      (is (= 1 @(stream/try-take! output2 -1 1000 -2)))
      (is (= 3 @(stream/try-take! output2 -1 1000 -2)))
      (is (= 5 @(stream/try-take! output2 -1 1000 -2)))
      (is (= 7 @(stream/try-take! output2 -1 1000 -2)))

      (is (= -2 @(stream/try-take! output2 -1 1000 -2)))

      (is (= 2 @(stream/try-take! output3 -1 1000 -2)))
      (is (= 4 @(stream/try-take! output3 -1 1000 -2)))
      (is (= 6 @(stream/try-take! output3 -1 1000 -2)))
      (is (= 8 @(stream/try-take! output3 -1 1000 -2)))
      (is (= -2 @(stream/try-take! output3 -1 1000 -2))))

    (system/stop!)))
