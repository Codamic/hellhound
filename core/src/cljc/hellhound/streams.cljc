(ns hellhound.streams
  (:require
   [clojure.core.async             :as async]
   [hellhound.streams.protocols    :as impl]
   [hellhound.utils             :refer [todo]]
   [hellhound.streams.impl.channel]))

;; Protocol Functions ---------------------------------------------------------
(def consume impl/consume)
(def put!    impl/put!)
(def connect impl/connect)
(def connect-via impl/connect-via)
(def close!  impl/close!)

;; Public API -----------------------------------------------------------------
(defn stream
  "An alias to core async channels."
  ([]
   (todo "Get the default buffer size from the config")
   (stream 100))
  ([buffer-or-size]
   (async/chan buffer-or-size)))

(defn sliding-stream
  "Create a core async channl with a sliding buffer."
  [buffer-size]
  (async/chan (async/sliding-buffer buffer-size)))

(defn dropping-stream
  "Create a core async channl with a dropping buffer."
  [buffer-size]
  (async/chan (async/dropping-buffer buffer-size)))

(defn stream?
  "Returns true if the given value can be used as a stream."
  [x]
  (and
   (satisfies? impl/Consumable x)
   (satisfies? impl/Sinkable x)
   (satisfies? impl/Connectable x)))

(comment
  (stream? (async/chan 10))

  (let [s (stream)]
    (consume #(println (str "SSSSS: " %)) s)
    (async/>!! s "sameer")
    (async/>!! s "samsam")))
