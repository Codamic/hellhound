(ns hellhound.message
  "HellHound components communicate with each other via their input and
  output streams by passing values to each other that satisfy `Message`
  protocol (checkout `hellhound.message.protocols` for more information).

  By default HellHound extends only IPersistentMap with the protocol
  mentioned above. So by default each message is a hashmap which has
  a unique **ID** attached to it. Also, Each message has a stack that
  is dedicated to store a set of functions known as **resolvers**.
  A resolver is a function which is going to get called in order (FILO)
  when the workflow of the message has ended. The purpose of resolvers
  are to provide a simple solution for async calls on several components.

  For example imaging a web server component which wants to send the
  response to the client. It need to send the request to the output
  stream and based on the system workflow the request would go through
  several components and gather the necessary data for the response.
  But the webserver component should be able to wait for the response
  data to be available to it. There is two solution for this problem.

  The first solution is to use a `hellhound.async/deferred` as the
  response and put it along side of the request in the message.
  and resolve all deferred values in the incomming messages with
  the response data in the incoming message. So in this case
  we would have loop in our workflow which is quite fine.

  The other solution is to return a `hellhound.async/deferred` again
  but instead of resolving it later based on incoming messages of
  the component it can set a resolver for that deferred value in the
  message itself. So when the workflow ends the resolver will automatically
  resolve the deferred value with the message as the argument.

  For source component which is the entry point of a workflow the first
  solution seems like a better idea but for a component which is
  in the middle of a workflow second solution is much simpler and nicer.
  "
  (:require
   [hellhound.async :as async]
   [hellhound.message.protocols :as impl]
   [hellhound.message.impl.message]))


(def id impl/id)
(def resolvers impl/resolvers)
(def type impl/type)
(def payload impl/payload)
(def enqueue-resolver impl/enqueue-resolver)
(def resolve! impl/resolve!)


(defn create
  "Creates A new Message with the given `initial-value`."
  ([]
   (create {}))
  ([initial-value]
   (impl/init initial-value)))


(comment
  (impl/resolvers (enqueue-resolver (create) #(println %)))
  (let [m (create)]
    (impl/resolve! (impl/enqueue-resolver m #(println %)))))
