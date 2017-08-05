(ns hellhound.system.component)


(extend-protocol protocols/Component
  clojure.lang.PersistentArrayMap
  (start! [this])
  (components [this] (get-components this)))
