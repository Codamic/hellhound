(ns hellhound.stream
  (:require
   [clojure.core.async :as async]
   [hellhound.utils    :refer [todo]]))

(defn stream
  "An alias to core async channels."
  ([]
   (todo "Get the default buffer size from the config")
   (stream 100))
  ([buffer-size]
   (async/chan buffer-size)))

(defn sliding-stream
  "Create a core async channl with a sliding buffer."
  [buffer-size]
  (async/chan (async/sliding-buffer buffer-size)))

(defn dropping-stream
  "Create a core async channl with a dropping buffer."
  [buffer-size]
  (async/chan (async/dropping-buffer buffer-size)))

(defn consume
  [f c]
  (async/go-loop []
    (let [v (async/<! c)]
      (f v)
      (recur))))


(comment
  (let [s (stream)]
    (consume #(println (str "SSSSS: " %)) s)
    (async/>!! s "sameer")
    (async/>!! s "samsam")))
