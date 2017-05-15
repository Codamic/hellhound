(ns hellhound.db.adapters.core)

(defprotocol DatabaseAdapter
  "This protocol represent an interface for different database adapters
  which `HellHound` uses to manage migrations. Basically this interface
  abstract away what migration system needs for its operations."
  (create-table [name])
  (insert-migration [migration-name status]))
