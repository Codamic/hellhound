(ns hellhound.components.db
  "Any database component should implement the `DatabaseLifecyle` protocol.
  This way **HellHound** Can manage Database accordingly to the implementation
  of the mentioned protocol. For example in order to setup the database,
  **HellHound** uses the `setup` function of the implementated protocol.")

(defprotocol DatabaseLifecycle
  (start    [component])
  (stop     [component])
  (create   [component])
  (setup    [component])
  (teardown [component]))
