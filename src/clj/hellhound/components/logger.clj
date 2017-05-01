(ns hellhound.components.logger
  (:require [hellhound.logger.core :refer :all]
            [hellhound.components.core :as component]
            [clojure.core.async        :as async]))


(defrecord Logger []
  component/Lifecycle
  (start [this]
    (let [log-chan (async/chan 1000)]
      (start-logger log-chan)
      (assoc this :channel log-chan)))

  (stop [this]
    (if (:channel this)
      (do
        (stop-logger (:channel this))
        (dissoc this :channel))
      this)))


(defn new-logger
  "Create a new logger instance from `Logger` component."
  []
  (->Logger))
