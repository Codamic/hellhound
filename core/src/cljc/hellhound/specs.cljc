(ns hellhound.specs
  (:require
   [clojure.spec.alpha :as s]))

(defn validate
  [spec data msg]
  (when-not (s/valid? spec data)
    (throw (ex-info msg (s/explain-data spec data)))))
