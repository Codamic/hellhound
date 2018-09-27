(ns ^{:clojure.tools.namespace.repl/load false} hellhound.system.protocols)


(defprotocol Splitter
  (connect [this sink operation-map]
    "Setup the `sink` channel to be connected to a source later based on
     the given `operation-map` which basically defines all the operations
     that should apply to the value before sending it to the sink. Operations
     like filter and map.")

  (commit
    [_]
    "Connect source channel to all the sinks")

  (close! [_]
    "Disconnects all the managing sinks and sources."))


(defprotocol ComponentManagement
  (components-vector
    [_]
    "Returns the raw definition of system components as a vector of components")
  (components-map
    [_]
    "Returns a map of components name to valid components.")
  (get-component
    [_ component-name]
    "Returns a component with the given `name` from initialized system.")

  (update-component
    [system component-name component]
    "Update the given component with the given name on component-map in the system.")

  (make-components-map
    [_]
    "Create a mapping from components name to components and validates each component."))

(defprotocol WorkflowManagement
  (get-workflow
    [system]
    "Returns the workflow of the given system"))

(defprotocol SystemManagement
  (update-system
    [system k v]
    "Updates the value of `k` with the given `v` in the given `system`."))

(defprotocol ExecutionManagement
  (execution-pool
    [system]
    "Returns the main thread pool of the system which is responsible for
     executing none blocking code. Nil in case of a single thread system.")

  (wait-pool
    [system]
    "Returns the wait thread pool of the system which is responsible for
     executing blocking code. Nil in case of a single thread system.")

  (schedule-pool
    [system]
    "Returns the schedule thread pool of the system which is responsible for
     scheduling code to be run in future. Nil in case of a single thread
     system.")

  (execution-mode [system]
    "Returns a keyword describing the execution model of the system. Possible
     values are ':single-threaded' and 'multi-threaded'"))
