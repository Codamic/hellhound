(ns hellhound.components.pretty-printer
  (:require
   [hellhound.logger :as logger]
   [hellhound.component :refer [deftransform]]))

(deftransform printer
  [component v]
  (logger/debug "PrettyPrinter-START: -----------------------------------------")
  (clojure.pprint/pprint v)
  (logger/debug "PrettyPrinter-END: -------------------------------------------"))
