(ns hellhound.http.ring.response
  "This namespace contains several helper function to
  help users to produce a ring response."
  (:require [ring.util.response :as res]))



(defn- content-type-of
  [res-format]
  (when-not (nil? res-format)
    (cond
      (= res-format :json) "application/json"
      (= res-format :xml)  "application/xml"
      (= res-format :html) "text/html"
      :else (str res-format))))

(defn html
  [body opts]
  {:status (or (:status opts) 200)
   :headers (merge {"Content-Type" "text/html"}
                   (or (:headers opts) {}))
   :body body})
