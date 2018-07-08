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
  (io? [component]
    "Returns a boolean indicating whether the component
     contains IO operation or not.")
  (consumer-fn [component]
    "Returns a function that would consumes value from the input
     and return a value which should be pass down the pipeline."))
