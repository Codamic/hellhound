(ns hellhound.system.core_test
  (:require [hellhound.system.core :as sut]
            [clojure.test :as t]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [hellhound.test :as ht]
            [clojure.spec.test.alpha :as stest]))


;; (t/deftest get-components
;;   (let [check-results (stest/check `hellhound.system.core/get-components)
;;         passed       (every? nil? (map :failure check-results))]
;;     (t/is passed (first check-results))))

(defn check
  [x]
  (let [check-results (stest/check x)
        passed       (every? nil? (map :failure check-results))]
    (t/is passed
          ;;(first check-results)
          x)))

;; (t/deftest all-the-spec
;;   (doseq [sym (stest/enumerate-namespace 'hellhound.system.core)]
;;     (t/testing (str "Checking specs of " sym)
;;       (check sym))))


;; (t/deftest all-the-spec
;;   (doseq [sym (stest/enumerate-namespace 'hellhound.system.core)]
;;     (t/testing (str "Checking specs of " sym)
;;       (check sym))))


;; (defn test-fn
;;   [sym-or-syms opts]
;;   `(fn []
;;      (let [check-results# (clojure.spec.test.alpha/check ~sym-or-syms ~opts)
;;            checks-passed?# (every? nil? (map :failure check-results#))]
;;        (println "---------------------------------")
;;        (println checks-passed?#)
;;        (if checks-passed?#
;;          (clojure.test/do-report {:type    :pass}
;;                                  :message (str "Generative tests pass for "
;;                                                (clojure.string/join ", " (map :sym check-results#))))
;;          (doseq [failed-check# (filter :failure check-results#)
;;                  :let [r# (clojure.spec.test.alpha/abbrev-result failed-check#)
;;                        failure# (:failure r#)]]
;;            (println "------------")
;;            (clojure.test/do-report
;;             {:type     :fail
;;              :message  (with-out-str (clojure.spec.alpha/explain-out failure#))
;;              :expected (->> r# :spec rest (apply hash-map) :ret)
;;              :actual   (if (instance? Throwable failure#)
;;                          failure#
;;                          (:clojure.spec.test.alpha/val failure#))})))
;;        checks-passed?#)))


(defmacro defspec-test
  ([name] `(defspec-test ~name nil))
  ([name opts]
   (when clojure.test/*load-tests*
     `(def ~(vary-meta name assoc :test `(hellhound.system.core_test/check ~name))
        (fn [] (clojure.test/test-var (var ~name)))))))

(doseq [sym (clojure.spec.test.alpha/enumerate-namespace 'hellhound.system.core)]
  (defspec-test sym))
