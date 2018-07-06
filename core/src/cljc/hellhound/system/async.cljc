(ns hellhound.system.async
  (:require
   [hellhound.async            :as async]
   [hellhound.system.store     :as store]
   [hellhound.system.execution :as exec]))

(defn execute-io!
  [f]
  ;;   (exec/execute-io-with-system  f)
  (async/future-with (exec/wait-pool (store/get-system)) (f)))

(defn execute
  [f]
  ;;(exec/execute-with-system (hellhound/system) f)
  (async/future-with (exec/execution-pool (store/get-system)) (f)))
