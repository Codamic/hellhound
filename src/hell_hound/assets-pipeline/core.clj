(ns hell-hound.assets-pipeline.core)

(defn middleware
  "A middleware to manage the assets of the handler"
  [handler]
  (fn [request]) handler)
