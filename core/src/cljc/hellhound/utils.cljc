(ns hellhound.utils
  (:require
   [hellhound.logger.formatters  :as formatter]
   [hellhound.env :as env]))

(defn code-location
  [and-form]
  (formatter/green
   (str *ns* ":" (:line (meta and-form)))))

(defn- printer
  [x]
  `(println ~(formatter/yellow (str x)) ":" ~x))


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


(defmacro dump
  [& args]
  (let [prints (map printer args)]
    `(do
       (println (str "Debug in "
                     ~(code-location &form)
                     " -----------------------"))
       ~@prints
       (println "End =========================\n"))))
