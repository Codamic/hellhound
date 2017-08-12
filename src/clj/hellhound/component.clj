(ns hellhound.component
  (:require
   [hellhound.system.protocols :as protocols]))


(extend-protocol protocols/IComponent
  clojure.lang.IPersistentMap
  (start! [this context]
    (let [start-fn (::start-fn this)]
      (assoc (start-fn this context)
             ::started? true)))

  (stop! [this]
    (let [stop-fn (::stop-fn this)]
      (stop-fn this)))

  (started? [this]
    (or (::started? this) false))

  (get-name [this]
    (::name this))

  (dependencies [this]
    (::depends-on this)))
