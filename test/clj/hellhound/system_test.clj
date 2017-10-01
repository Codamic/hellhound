(ns hellhound.system-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t :refer [deftest testing is are]]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
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
(defn sample-start-fn
  [key value]
  (fn [component context]
    (assoc component
           key value
           :time (time-coerce/to-long (time/now)))))


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
      (sample-stop-fn :key2))]})


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
        (is (< (:time component1) (:time component2))))
      (testing "Workflow"
        (let [input1 (:hellhound.component/input component1)
              input2 (:hellhound.component/input component1)])
        (is (not (nil? input1)))
        (is (stream/stream? input1))))))



(t/run-tests)
