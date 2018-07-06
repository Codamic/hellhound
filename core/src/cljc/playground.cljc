(ns playground
  (:require
   [hellhound.core :as hh]
   [hellhound.system :as sys]
   [hellhound.component :as com]
   [clojure.java.io :as io]))

(defn c1
  [this ctx]
  (let [[in out] (com/io this)]
    (doseq [v (range 100)]
      (streams/>> input v)))
  this)

(defn c2
  [this ctx]
  (com/input->output
   this
   (fn [v]
     (System/sleep 5000)
     (inc v)))
  this)

(defn c3
  [this ctx]
  (let [[input output] (com/io this)]
    (streams/consume in #(println (str ">>> " %)))
    this))

(def system
  {:components [(make-component ::c1 c1 (fn [this] this) [])
                (make-component ::c2 c2 (fn [this] this) [])
                (make-component ::c3 c3 (fn [this] this) [])]

   :workflow [[::c1 ::c2]
              [::c2 ::c3]]})


(sys/set-system! system)
(sys/start!)
(sys/stop!)
