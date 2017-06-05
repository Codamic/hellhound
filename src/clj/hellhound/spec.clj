(ns hellhound.spec
  "Utility namespace for spec management and validation in
  HellHound."
  (:require [clojure.spec.alpha :as spec]))


(defn throw-exception
  [spec value]
  (throw
   (ex-info (format "Given value does not follow the `%s` spec.\n"  spec)
            {:provided-value value
             :expected-spec  spec
             :explain        (spec/explain-data spec value)})))

(defn validate
  [spec value]
  (if-not (spec/valid? spec value)
    (throw-exception spec value)
    value))
