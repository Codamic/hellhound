(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha :as s]
   [bidi.ring          :as bring]
   [manifold.stream    :as stream]
   [manifold.deferred  :as deferred]
   [aleph.http         :as http]
   [hellhound.logger   :as log]
   [hellhound.core     :as hellhound]))

(defn hello
  [req]
  {:status 200
   :headers []
   :body "Yeah"})

(def event-router
  {:hello #(stream/put! (:stream %) "hey!")})

(defn resolve-route
  [input-stream router msg]
  (let [handler (:hello router)]
    (handler {:stream input-stream :msg msg})))

(defn setup-event-router
  [router-stream router]
  (stream/consume #(resolve-route router-stream event-router %) router-stream))

(defn create-ws
  [req]
  (-> (http/websocket-connection req)
      (deferred/catch Exception #(throw %))))

(defn ws
  [req]
  (log/info "Accpting WS connection")
  (let [input-stream  (create-ws req)
        router-stream (stream/stream 100)
        output-stream (stream/stream 100)]

    (stream/consume #(println %) output-stream)
    (setup-event-router router-stream)
    (stream/connect input-stream router-stream)
    (stream/connect router-stream output-stream)))


(def routes
  (bring/make-handler
   ["/"
    {:get {"/" hello
           "ws/" ws}}]))



;; (bidi/match-route hellhound-routes "/")
;; (bidi/path-for hellhound-routes :something)
