(ns hellhound.utils
  (:require
   [hellhound.env :as env]))


(defn uuid
  "Generated and return a random uuid."
  []
  (java.util.UUID/randomUUID))


(defmacro todo
  "Very simple TODO macro to reminds us of the work
  we should do. It will do nothing on production and I
  intentionally separated it from the logger."
  [msg]
  (if (or (env/test?) (env/development?))
    `(println (str "[" ~*ns* ":" ~(:line (meta &form))
                   "] TODO: " ~msg))))
