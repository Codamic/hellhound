(ns hellhound.components.pedestal
  "This namespace contains the Pedestal component which is one of
  the most important components in the HellHound ecosystem.
  `Pedestal` component is responsible for creating a pedestal service
  by reading the configuration and service map from environment config
  file or from the given input."
  (:require
   [io.pedestal.http               :as http]
   [hellhound.core                 :as hellhound]
   [hellhound.components.protocols :as protocols]))

(declare map->Pedestal)

;; Functions -----------------------------------------------
(defn test?
  []
  (hellhound/test?))


(defn new-pedestal
  "Creates a new pedestal record."
  [service-map service]
  (map->Pedestal {:service-map service-map
                  :serive      service}))

(defn make-pedestal-component
  "Create new pedestal record to with the key name of `:pedestal`
  to be injected into `hellhound`'s system map"
  [system-map service-map service]
  (update-in system-map [:components :pedestal]
             (new-pedestal service-map sercice)))

;; Record --------------------------------------------------
(defrecord Pedestal [service-map service]
  protocols/Lifecycle
  (start [this]
    (if service
      this
      (cond-> service-map
        true                      http/create-server
        (not (test? service-map)) http/start
        true                      ((partial assoc this :service)))))

  (stop [this]
    (when (and service (not (test? service-map))))
    (http/stop service)
    (assoc this :service nil)))
