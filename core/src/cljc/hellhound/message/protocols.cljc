(ns hellhound.message.protocols
  {:clojure.tools.namespace.repl/load false})


(defprotocol Message
  (init [_]
    "Initialize the the message.")
  (id [_] "Returns the ID of the given value.")
  (type [_] "Returns the type of the given value.")
  (resolvers [_]
    "Returns a sequence of resolver functions which each of them resolves
     a deferred value.")
  (enqueue-resolver [_ r] "Adds the given resolver to the resolvers queue.")
  (resolve! [_] "Resolves all the resolvers in queue in a FILO fashion."))
