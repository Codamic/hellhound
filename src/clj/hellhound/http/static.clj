(ns hellhound.http.static
  "All the necessary functions to serve static resources from a hellhound
  application live here."
  (:require [io.pedestal.http.ring-middlewares :as ring-middlewares]
            [hellhound.core :as hellhound]))

(defn serve-resource
  "Server the content of given path from the resource path."
  [context]
  (let [serve-path (hellhound/fetch-config-key :public-files-path)]
    (fn []) (ring-middlewares/resource serve-path)))
