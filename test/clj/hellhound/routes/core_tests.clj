(ns hellhound.routes.core-tests
  (:require [hellhound.routes.core :as sut]
            [clojure.test :refer :all]))

(testing :not-found
  (let [result (sut/not-found {})]
    (is (= (:status result) 404))))

(testing :hellhound-routes
  (let [result (sut/hellhound-routes)]
    (is (= (count result) 2))
    (is (= (count (get result 1)) 2))))
