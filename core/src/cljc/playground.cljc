(ns playground
  (:require
   [hellhound.core :as hh]
   [hellhound.system :as sys]
   [hellhound.streams :as streams]
   [hellhound.component :as com]))


(defn c1
  [this ctx]
  (let [[in out] (com/io this)]
    (doseq [v (range 100)]
      (streams/>> in v)))
  this)

(defn c2
  [this ctx]
  (com/input->output
   this
   (fn [v]
     (Thread/sleep 5000)
     (inc v)))
  this)

(defn c3
  [this ctx]
  (let [[in out] (com/io this)]
    (streams/consume in #(println (str ">>> " %)))
    this))

(def system
  {:components [(com/make-component ::c1 c1 (fn [this] this) [])
                (com/make-component ::c2 c2 (fn [this] this) [])
                (com/make-component ::c3 c3 (fn [this] this) [])]

   :workflow [[::c1 ::c2]
              [::c2 ::c3]]})


(sys/set-system! system)
(sys/start!)
(sys/stop!)
