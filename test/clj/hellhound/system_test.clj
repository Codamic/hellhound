(ns hellhound.system-test
  (:require [hellhound.system :as system :refer [defcomponent]]
            [clojure.spec.alpha :as s]
            [clojure.test :as t :refer [deftest testing is are]]))

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
    (assoc component key value)))

(defn sample-stop-fn
  [key]
  (fn [component]
    (dissoc (assoc component :stopped? true) key)))

(def sample-system
  {:components
   [(defcomponent :sample/component
      (sample-start-fn :key1 :value1)
      (sample-stop-fn :key1))]})

(deftest system-test
  (testing "smallest working system"
    (system/set-system! sample-system)
    (system/start!)
    (let [subject     (system/system)
          component   (:sample/component (:components subject))]
      (is (not (nil? component)))
      (is (= :value1 (:key1 component))))))


(t/run-tests)
