(ns hellhound.logger.middlewares)


(defn exceptions
  "A logger middleware function which transforms data to a
  more understandable format for formatters to deal with
  exceptions."
  [data]
  (if (= clojure.lang.ExceptionInfo (class data))
    (assoc data :is-exception? true)
    data))
