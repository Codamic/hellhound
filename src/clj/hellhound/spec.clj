(ns hellhound.spec
  "Utility namespace for spec management and validation in
  HellHound."
  (:require [clojure.spec.alpha :as s]))

(defn throw-exception
  [spec value msg]
  (throw
   (ex-info (format "%s. Given value does not follow the `%s` spec.\n" msg spec)
            {:provided-value value
             :expected-spec  spec
             :explain        (s/explain-data spec value)})))

(defn validate
  [spec value msg]
  (when-not (s/valid? spec value)
    (throw-exception spec value msg)))
