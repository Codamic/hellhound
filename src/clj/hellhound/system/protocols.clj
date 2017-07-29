(ns hellhound.system.protocols)

(defprotocol System
  (start [this]
    "Starts the system.")

  (stop  [this]
    "stops the system")

  (restart [this]
    "Restarts the system")

  (get-component [this component-name]
    "Return the instance of the component with the given name"))
