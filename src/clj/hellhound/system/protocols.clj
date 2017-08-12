(ns hellhound.system.protocols
  "All the components of hellhound system should implements `IComponent`
  protocol. Either by extending a type of by using a record.")


(defprotocol IComponent
  "This protocol defines a very basic component for hellhound system."
  (start! [component context]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started? [component]
    "Returns a `true` if component started and `false` otherwise.")

  (get-name [component]
    "Returns the name of the component.")

  (dependencies [component]
    "Returns a vector of dependency names."))
