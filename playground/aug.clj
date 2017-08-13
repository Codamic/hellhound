(ns aug
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))


(defn dev
  [x i]
  (when-not (= i 0)
    (/ x i)))

(s/fdef dev
        :args (s/cat :x int? :i int?)
        :ref int?
        :fn #(= (* (:ret %) (:i (:args %)))
                (:x (:args %))))



(s/exercise-fn `dev)
(->> (stest/check `dev)
     stest/summarize-results)

(s/explain `dev)
(stest/check `dev)
