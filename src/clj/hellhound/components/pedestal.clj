(ns hellhound.components.pedestal
  "This namespace contains the Pedestal component which is one of
  the most important components in the HellHound ecosystem.
  `Pedestal` component is responsible for creating a pedestal service
  by reading the configuration and service map from environment config
  file or from the given input."
  (:require
   [io.pedestal.http               :as http]
   [hellhound.core                 :as hellhound]
   [hellhound.components           :as components]
   [hellhound.components.protocols :as protocols]))

(declare map->Pedestal)

;; Functions -----------------------------------------------
(defn new-pedestal
  "Creates a new pedestal record."
  [service-map]
  (map->Pedestal {:service-map service-map}))


(defn make-instance
  "Create new pedestal instance to with the key name of `:pedestal`
  to be injected into `hellhound`'s system map"
  ([service-map]
   (make-instance service-map {}))

  ([service-map options]
   (let [{:keys [requirements inputs]} options]
     {:pedestal (components/create-instance
                 (new-pedestal service-map)
                 requirements
                 inputs)})))

;; Record --------------------------------------------------
(defrecord Pedestal [service-map]
  protocols/Lifecycle
  (start [this]
    (cond-> service-map
      true                      http/create-server
      (not (hellhound/test?))   http/start
      true                      ((partial assoc this :service))))

  (stop [this]
    (when (and (:service this) (not (hellhound/test?)))
      (http/stop (:service this))
      (assoc this :service nil))))
