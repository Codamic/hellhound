(ns hellhound.http.statics
  "All the necessary functions to serve static resources from a hellhound
  application live here."
  (:require [io.pedestal.http.ring-middlewares :as ring-middlewares]))

(defn serve-resource
  "Server the content of given path from the resource path."
  []
  (let [serve-path])
  (fn [] (ring-middlewares/resource path)))
