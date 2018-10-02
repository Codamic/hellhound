(ns hellhound.config-test
  (:require
   [clojure.test :as t :refer [deftest testing is are]]
   [hellhound.system :as sys]
   [hellhound.config :as sut]))

(deftest get-config-from-system
  (are [x y] (= x y)
    nil (sut/get-config-from-system {} :e)
    nil (sut/get-config-from-system {} :a :b)
    true (sut/get-config-from-system {:example true} :example)
    false (sut/get-config-from-system {:example {:key1 false}} :example :key1))
  (with-redefs [hellhound.config.helpers/default-value-for (fn [x] :example-value)]
    (is (nil? (sut/get-config-from-system {:example {:key1 nil}} :example :key1)))))

(deftest get-config
  (sys/set-system! (fn [] {:example {:key1 true :key2 "test"}}))
  (are [x] nil?
    (sut/get-config :e)
    (sut/get-config :e :a)
    (sut/get-config :example :key3))

  (are [x y] (= x y)
    true (sut/get-config :example :key1)
    "test" (sut/get-config :example :key2)
    {:key1 true :key2 "test"} (sut/get-config :example)))

(comment
  (t/run-tests))
