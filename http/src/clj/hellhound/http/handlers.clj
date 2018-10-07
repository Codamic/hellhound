(ns hellhound.http.handlers
  "A collection of useful Ring handlers to be used
  with a Ring router. In case of HellHound, `hellhound.http.route`."
  (:require
   [hellhound.http.utils :as utils]
   [hellhound.http.response :as response]
   [hellhound.streams :as streams]))


(defn not-found
  "A handler which gets a `request` map and returns a ring response map
  with status of 404."
  [req]
  {:status 404 :headers {} :body "NOT FOUND!"})

(defn default-handler
  "Given a `request` map it will return an empty responst map with status
  of 200."
  [req]
  {:status 200
   :headers []})

(defn upgraded
  "A function which gets a `request` map and returns a response map
   with 101 status to upgrade the connection to websocket."
  [req]
  {:status 101})

(defn bad-request
  ([context]
   (bad-request context ""))
  ([{:keys [request]} msg]
   {:status 400
    :headers {"content-type" "application/text"}
    :body msg}))


(defn req-to-stream
  [context]
  (let [out (:output context)]
    (fn [req]
      (let [res (response/async-response (or (:config context) {}))]
        (streams/>> out
                    {:request req
                     :hellhound.http.request/id (utils/uuid)
                     :response {}
                     :response-deferred res})
        res))))
