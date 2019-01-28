(ns hellhound.components.ring-middleware)


(defn middleware-applier
  [middlewares]
  (fn [component value]
    (let [request (:request value)])))

(defn make-ring-middleware-component
  [name middlewares]
  (make-transformer name (middleware-applier middlewares)))
