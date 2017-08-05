(ns hellhound.system.protocols)

(defprotocol System
  (start [this]
    "Starts the system.")

  (stop  [this]
    "stops the system")

  (restart [this]
    "Restarts the system")

  (components [this]
    "Returns a hashmap of all the components defined in the system")

  (get-component [this component-name]
    "Return the instance of the component with the given name"))



(defprotocol Component
  "This protocol defines a very basic component for hellhound system."
  (start! [component]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started?
    [component])
  "Returns a `true` if component started and `false` otherwise."

  (dependencies [component]
    "Returns a vector of dependency names."))
