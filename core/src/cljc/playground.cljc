(ns playground
  (:require
   [hellhound.core :as hh]
   [hellhound.system :as sys]
   [hellhound.streams :as streams]
   [hellhound.component :as com :refer [defcomponent deftransform! deftransform]]))


(defcomponent c1
  [this in out]
  (doseq [v (range 100)]
    (streams/>> in v))
  this)

(deftransform c2
  [this v]
  (Thread/sleep 5000)
  (inc v))

(deftransform c3
  [this v]
  (println (format "GOT: %s" v))
  nil)


(def system
  {:components [c1 c2 c3]
   :workflow [[::c1 ::c2]
              [::c2 ::c3]]
   :execution {:mode :multi-thread}})


(sys/set-system! system)
(sys/start!)
(sys/stop!)
