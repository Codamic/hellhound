(ns hellhound.component-test
  (:require [hellhound.component :as sut]
            [clojure.test :as t :refer [deftest testing is are]]))


;; make-component test ---------------------------------------
(def simple-map {:hellhound.component/name       :component/name
                 :hellhound.component/start-fn   inc
                 :hellhound.component/stop-fn    inc
                 :hellhound.component/depends-on []})

(deftest make-component-test
  (testing "make-component"
    (is (= simple-map (sut/make-component :component/name inc inc)))
    (is (= simple-map (sut/make-component :component/name inc inc [])))))
