(ns hellhound.components.pedestal
  "This namespace contains the Pedestal component which is one of
  the most important components in the HellHound ecosystem.
  `Pedestal` component is responsible for creating a pedestal service
  by reading the configuration and service map from environment config
  file or from the given input."
  (:require [hellhound.components.protocols :as protocols]))

(defrecord Pedestal []
  protocols/Lifecycle
  (start [this])
  (stop [this]))
