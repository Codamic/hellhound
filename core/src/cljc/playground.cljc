(ns playground
  (:require
   [hellhound.core :as hh]
   [hellhound.system :as sys]
   [hellhound.system.core :as syscore]
   [hellhound.streams :as streams]
   [hellhound.component :as com :refer [defcomponent deftransform! deftransform]]))


(defcomponent c1
  [this]
  (let [[in out] (com/io this)]
    (hellhound.system.async/execute-io!
     #(loop [v 0]
        (Thread/sleep 300)
        (streams/>> out v)
        (when (and (not (streams/closed? in))
                   (< 100 v))
          (recur (inc v))))))
  this)


(defcomponent c2
  [this]
  (let [[in out] (com/io this)]
    (streams/consume #(streams/>> out (inc %)) in))
  this)


;; (deftransform c2
;;   [this v]
;;   (Thread/sleep 5000)
;;   (println "xxx" v)
;;   (inc v))

(deftransform c3
  [this v]
  (println (format "GOT: %s" v))
  nil)


(def system
  {:components [c1 (assoc c2 :hellhound.component/depends-on [::c1])
                   (assoc c3 :hellhound.component/depends-on [::c1])]
   :workflow [[::c1 ::c2]
              [::c2 ::c3]]
   :execution {:mode :multi-thread}})



(sys/set-system! system)
;;(sys/start!)
(comment
  (clojure.pprint/pprint (sys/system))
  (sys/stop!))
