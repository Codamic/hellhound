(ns hellhound.streams
  (:require [clojure.core.async :as async]))


(defn connect
  [c1 c2])

(defn consume
  [channel f])

(defn consume-async
  [channel f])

(defn put!
  [channel value])

(defn take!
  [channel value])


(comment
  (let [a (async/chan 10)
        b (async/chan 10)
        c (async/chan 10)
        read #(async/go-loop []
               (let [v (async/<! %1)]
                 (println (format "GO-%s: %s" %2 v)))
               (recur))]

    (comment
      (let [c (async/chan 10)]

        (read c "1")
        (read c "2")

        (doseq [x [1 2 3 4 5 6 7 8 9 10 11 12 13]]
          (async/>!! c x))))


    (comment
      (let [a (async/chan 10)
            b (async/chan 10)
            c (async/chan 10)]
        (async/pipe a b)
        (async/pipe a c)

        (read b "b")
        (read c "c")

        (doseq [x [1 2 3 4 5 6 7 8 9 10 11 12 13]]
          (async/>!! a x))))

    (comment
      (let [a (async/chan 10)
            b (async/chan 10)
            c (async/chan 10)]
        (read b "b")
        (read c "c")

        (async/go-loop []
          (let [x (async/<! a)]
            (async/>! b x)
            (async/>! c x)
            (recur)))

        (doseq [x [1 2 3 4 6 7 8 9 10 11 12 13]]
          (async/>!! a x))))
    (let [m (async/mult a)]
      (read b "b")
      (read c "c")
      (async/tap m b)
      (async/tap m c)
      (doseq [x [1 2 3 4 5 6 7 8 9 10 11 12 13]]
          (async/>!! a x)))))
