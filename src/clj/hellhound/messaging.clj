(ns hellhound.messaging
  "This namespace contains all the necessary means to handle client
  side events.")

(defmacro defrouter
  "Create an event router to be used for dispatching client events"
  [name body]
  `(def ~name ~body))
