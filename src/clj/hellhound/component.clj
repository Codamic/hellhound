(ns hellhound.component)


(extend-protocol protocols/Component
  clojure.lang.PersistentArrayMap
  (start! [this]
    (let [start-fn (::start-fn this)]
      (start-fn this)))

  (stop! [this]
    (let [stop-fn (::stop-fn this)]
      (stop-fn this)))

  (started? [this]
    (or (::started? this) false))

  (dependencies [this]
    (::depends-on this)))
