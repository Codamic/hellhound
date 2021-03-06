(ns hellhound.system.protocols
  {:clojure.tools.namespace.repl/load false})



;; Splitter protocol describes all the rules to create an splitter type
;; which connects the output of a source component to the input of one
;; or more sink components.
(defprotocol Splitter
  (connect
    [this sink node]
    "Setup the `sink` channel to be connected to a source later based on
     the given `operation-map` which basically defines all the operations
     that should apply to the value before sending it to the sink. Operations
     like filter and map.")

  (commit
    [_]
    "Connect source channel to all the sinks")

  (close!
    [_]
    "Disconnects all the managing sinks and sources."))


;; This protocol describes how a system should manage its
;; components.
(defprotocol ComponentManagement
  (components
    [_]
    "Returns a map of components name to valid components.")
  (get-component
    [_ component-name]
    "Returns a component with the given `name` from initialized system.")

  (update-component
    [system component-name component]
    "Update the given component with the given name on component-map in the system.")

  (validate-components
    [_]
    "Validates each component in the components map."))


;; A system protocol for managing the workflow of the system.
(defprotocol WorkflowManagement
  (get-workflow
    [system]
    "Returns the workflow of the given system"))

;; Basic system actions described in this protocol.
(defprotocol SystemManagement
  (update-system
    [system k v]
    "Updates the value of `k` with the given `v` in the given `system`.")

  (get-value
    [system ks]
    [system ks default-value]
    "Returns the value of the given `ks` (keys) from the system and returns the
     default-value if the value was missing."))


;; This protocol explains how to manage the execution and thread pool creation
;; of the system
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
