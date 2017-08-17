(ns hellhound.test
  (:require [clojure.test :as t]
            [clojure.spec.test.alpha :as stest]
            [hellhound.system.core :as c]))

(defn check
  [x]
  (let [check-results (stest/check x)
        passed       (every? nil? (map :failure check-results))]
    ;; TODO: Instead of using is replace the following experssion with
    ;;       a clojure.test/do-report
    (t/is passed (first check-results))))


(defn ns-spec-tests
  [ns-sym]
  (doseq [[sym actual-var] (ns-publics ns-sym)
          :let [full-symbol  (symbol (name ns-sym) (name sym))]]
    (alter-meta! actual-var assoc :test #(check full-symbol))
    (t/test-var actual-var)))
