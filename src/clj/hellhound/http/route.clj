(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha :as s]
   [bidi.ring          :as bring]
   [manifold.stream    :as stream]
   [aleph.http         :as http]
   [hellhound.logger   :as log]
   [hellhound.core     :as hellhound]))

(defn hello
  [req]
  {:status 200
   :headers []
   :body "Yeah"})

(defn ws
  [req]
  (log/info "Accpting WS connection")
  (let [input-stream @(http/websocket-connection req)
        router-stream (stream/stream 100)]
    (stream/connect input-stream input-stream)))


(def routes
  (bring/make-handler
   ["/"
    {:get {"/" hello
           "ws/" ws}}]))



;; (bidi/match-route hellhound-routes "/")
;; (bidi/path-for hellhound-routes :something)
