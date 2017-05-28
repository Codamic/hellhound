(ns hellhound.components.logger
  "Hellhound logger component is a core.async style logger which
  uses a core.async thread for its business."
  (:require [hellhound.logger.core          :refer :all]
            [hellhound.components.protocols :as protocols]
            [clojure.core.async             :as async]))


(defrecord Logger []
  protocols/Lifecycle
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
