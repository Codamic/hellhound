(ns hellhound.components.logger
  "Hellhound logger component is a core.async style logger which
  uses a core.async thread for its business."
  (:require [hellhound.logger.core          :refer :all]
            [hellhound.components.protocols :as protocols]
            [clojure.core.async             :as async]))


(defrecord Logger []
  protocols/Lifecycle
  (start [this]
    (let [log-chan (async/chan 1000)
          tchannel (start-logger log-chan)]
      (merge this
             {:channel log-chan
              :thread-channel tchannel} )))

  (stop [this]
    (if (:thread-channel this)
      (do
        (stop-logger (:thread-channel this))
        (dissoc this :channel :thread-channel))
      this)))


(defn new-logger
  "Create a new logger instance from `Logger` component."
  []
  (->Logger))
