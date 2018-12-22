(ns hellhound.message.impl.message
  (:require
   [hellhound.utils :as utils]
   [hellhound.message.protocols :as protocols]))


(extend-type clojure.lang.IPersistentMap
  protocols/Message
  (init
    [msg]
    (merge msg {:hellhound.message/id (utils/uuid)
                :hellhound.message/resolvers (list)}))

  (id
    [msg]
    (:hellhound.message/id msg))

  (resolvers
    [msg]
    (:hellhound.message/resolvers msg))

  (enqueue-resolver
    [msg r]
    (assoc msg
           :hellhound.message/resolvers
           (conj (protocols/resolvers msg) r)))

  (resolve!
    [msg]
    (doseq [r (protocols/resolvers msg)]
      (r msg))
    (assoc msg :hellhound.message/resolvers (list))))
