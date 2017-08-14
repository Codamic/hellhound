(ns hellhound.test
  (:require [clojure.test :as t]
            [hellhound.system.core :as c]))

;; (def a (:clojure.spec.test.check/ret (first (clojure.spec.test.alpha/check 'hellhound.system.core/get-components))))
;; (keys a)
;; (:result a)
;; (:fail a)
;; (ex-data (:clojure.test.check.properties/error (:result-data a)))

(defmacro testspec
  [sym]
  `(clojure.test/deftest test-name#
     (clojure.test/testing (str "Testing spec for " (name ~sym))
       (println "sadasdasd"))))



(defmacro defspec-test
  ([name sym-or-syms] `(defspec-test ~name ~sym-or-syms nil))
  ([name sym-or-syms opts]
   (when clojure.test/*load-tests*
     `(def ~(vary-meta name assoc
                       :test `(fn []
                                (let [check-results# (clojure.spec.test.alpha/check ~sym-or-syms ~opts)
                                      checks-passed?# (every? nil? (map :failure check-results#))]
                                  (if checks-passed?#
                                    (clojure.test/do-report {:type    :pass}
                                                  :message (str "Generative tests pass for "
                                                                (clojure.string/join ", " (map :sym check-results#))))
                                    (doseq [failed-check# (filter :failure check-results#)
                                            :let [r# (clojure.spec.test.alpha/abbrev-result failed-check#)
                                                  failure# (:failure r#)]]
                                      (println "------------")
                                      (clojure.test/do-report
                                       {:type     :fail
                                        :message  (with-out-str (clojure.spec.alpha/explain-out failure#))
                                        :expected (->> r# :spec rest (apply hash-map) :ret)
                                        :actual   (if (instance? Throwable failure#)
                                                    failure#
                                                    (:clojure.spec.test.alpha/val failure#))})))
                                  checks-passed?#)))
        (fn [] (t/test-var (var ~name)))))))

(macroexpand-1 (testspec 'hellhound.system.core/get-components))
