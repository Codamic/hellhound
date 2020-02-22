(ns hellhound.system.development
  (:require
   [hellhound.system :as sys]
   [clojure.tools.namespace.repl :as repl]))


(defn load-and-set-system!
  [system-path]
  (assert (symbol? system-path) "'system-path' value must be a symbol")
  (assert (namespace system-path)
          "'system-path' value must be a namespace-qualified symbol")
  (let [system-fn (ns-resolve *ns* system-path)]
    (hellhound.system.store/set-system! system-fn)))

(defmacro setup-development
  [nsname system-var]
  `(do
     (defn ~(symbol "start")
       []
       (assert (symbol? ~system-var) "'system-path' value must be a symbol")
       (let [system-fn# (ns-resolve (quote ~nsname) ~system-var)]
         (hellhound.system.store/set-system! system-fn#))
       (hellhound.system/start!))

     (defn ~(symbol "stop")
       []
       (hellhound.system/stop!))

     (defn ~(symbol "restart")
       []
       (hellhound.system/stop!)
       (clojure.tools.namespace.repl/refresh :after  '~(symbol (str nsname "/start"))))))
