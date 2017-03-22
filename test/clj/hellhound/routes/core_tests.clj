(ns hellhound.routes.core-tests
  (:require [hellhound.routes.core :as sut]
            [clojure.test :refer :all]))

(deftest not-found
  (let [result (sut/not-found {})]
    (is (= (:status result) 404))))

(deftest hellhound-routes
  (let [result (sut/hellhound-routes)]
    (is (= (count result) 2))
    (is (= (count (get result 1)) 2))))
