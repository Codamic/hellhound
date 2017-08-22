(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha :as s]
   [bidi.ring          :as bring]
   [manifold.stream    :as stream]
   [manifold.deferred  :as deferred]
   [aleph.http         :as http]
   [hellhound.http.websocket.json :as jpack]
   [hellhound.http.websocket.core :as packer]
   [hellhound.logger   :as log]
   [hellhound.core     :as hellhound]))



(defn hello
  [req]
  {:status 200
   :headers []
   :body "Yeah"})

(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})

(def event-router
  {:hello (fn [x] (str "something" x))})

(defn setup-event-router
  [output router]
  (fn [msg]
    (let [handler (:hello router)]
      (stream/put! output (handler {:msg (jpack/unpack msg)})))))

(defn create-ws
  [req]
  (-> (http/websocket-connection req)
      (deferred/catch Exception #(throw %))))

(defn ws
  [req]
  (log/info "Accpting WS connection")
  (->
   (deferred/let-flow [socket (http/websocket-connection req)
                       router-stream (stream/stream 100)
                       output-stream (stream/stream 100)]

    (stream/connect-via socket (setup-event-router router-stream event-router) router-stream)
    (stream/connect router-stream output-stream)
    (stream/consume #(log/info %) output-stream))

   (deferred/catch
       (fn [err]
         (log/error err)
         non-websocket-request))))




(def routes
  (bring/make-handler
   ["/"
    {:get {"/" hello
           "ws/" ws}}]))



;; (bidi/match-route hellhound-routes "/")
;; (bidi/path-for hellhound-routes :something)
