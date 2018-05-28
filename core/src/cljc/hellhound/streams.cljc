(ns hellhound.streams
  (:require
   [clojure.core.async             :as async]
   [hellhound.streams.protocols    :as impl]
   [hellhound.utils             :refer [todo]]
   [hellhound.streams.impl.channel]))

;; Protocol Functions ---------------------------------------------------------
(def consume impl/consume)
(def take!   impl/take!)
(def try-take! impl/try-take!)

(def put!    impl/put!)
(def try-put! impl/try-put!)

(def connect impl/connect)
(def connect-via impl/connect-via)

(def close!  impl/close!)

(defmacro do-async
  "A dummy macro to create a level of indirection toword core async go macro.

  In order to have more control over go macro in the future we need a layer of
  indirection. Let's say in the future we decided to ditch core async. This way
  we can be backward compatible."
  [& body]
  `(clojure.core.async/go ~@body))

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
