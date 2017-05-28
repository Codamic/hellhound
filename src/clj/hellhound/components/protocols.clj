(ns hellhound.components.protocols)

(defprotocol Lifecycle
  "This protocol defines the structure of each component"
  (start [component]
    "The starting point for any component's life cycle.")
  (stop  [component]
    "The ending point of any component's life cycle."))

(defprotocol DatabaseLifecycle
  "Any database component should implement this protocol. This way
  **HellHound** can manage database accordingly to the implementation
  of this protocol. For example in order to create the database,
  **HellHound** uses the `setup` function of the implementated protocol."
  (start [component]
    "Starting point of the database component. Usually this function
     would be responsible for connecting to the database engine.")

  (stop [component]
    "Ending point for the commponent life cycle. Usually this function
     is responsible for disconnecting from the engine and cleaning up.")

  (setup [component]
    "This function should bootstrap the database and setup the necessary
     means for the database component to work. **HellHound** will call
     this function only for bootstrapping the database. One of the usual
     things to do in this function is to create the default database.

     See: `hellhound.tasks.db`")

  (teardown [component]
    "In order to clean up after initial database changes like creating
     a database or user or any similar action, **HellHound** will call
     this function."))
