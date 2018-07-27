(ns hellhound.components.defaults)

(defn start-fn
  [this ctx]
  (assoc this :context ctx))

(def stop-fn identity)
