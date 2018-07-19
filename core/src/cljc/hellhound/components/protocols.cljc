(ns hellhound.components.protocols)

(defprotocol IComponent
  "This protocol defines a very basic component for hellhound system."
  (initialize [component]
    "Returns the initialized component.")

  (start! [component context]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started? [component]
    "Returns a `true` if component started and `false` otherwise.")

  (get-name [component]
    "Returns the name of the component.")

  (dependencies [component]
    "Returns a vector of dependency names.")

  (input [component]
    "Returns the input stream of the component.")

  (output [component]
    "Returns the output stream of the component.")

  (get-fn [component]
    "Returns a function that get called after the start stage of the system.")

  (ready? [component]
    "Returns true if the component already started and the component `fn`
     has been called as well.")

  (mark-as-ready
    [component]
    "Marks the component as ready"))
