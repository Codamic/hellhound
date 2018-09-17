(ns hellhound.components.pretty-printer
  (:require
   [hellhound.component :refer [deftransform]]))

(deftransform printer
  [component v]
  (println "PrettyPrinter --------------------------------------------")
  (clojure.pprint/pprint v)
  (println))
