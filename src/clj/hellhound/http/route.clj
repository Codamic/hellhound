(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha :as s]
   [io.pedestal.http.route.prefix-tree :as prefix-tree]
   [io.pedestal.http.route.router :as router]
   [manifold.stream    :as stream]
   [manifold.deferred  :as deferred]
   [aleph.http         :as http]
   [io.pedestal.http.route    :as route]
   [hellhound.http.websocket.json :as jpack]
   [hellhound.http.websocket.core :as packer]
   [hellhound.logger   :as log]
   [hellhound.component :as hcomp]
   [hellhound.core     :as hellhound]))


(defrecord MapRouter [routes tree-map]
  router/Router
  (find-route [this req]
    ;; find a result in the prefix-tree - payload could contains mutiple routes
    (when-let [match-fn (tree-map (:uri req))]
      ;; call payload function to find specific match based on method, host, scheme and port
      (when-let [route (match-fn req)]
        ;; return a match only if query constraints are satisfied
        (when ((::prefix-tree/satisfies-constraints? route) req nil) ;; the `nil` here is "path-params"
          route)))))

(defn matching-route-map
  "Given the full sequence of route-maps,
  return a single map, keyed by path, whose value is a function matching on the req.
  The function takes a request, matches criteria and constraints, and returns
  the most specific match.
  This function only processes the routes if all routes are static."
  [routes]
  {:pre [(not (some prefix-tree/contains-wilds? (map :path routes)))]}
  (let [initial-tree-map (group-by :path
                                   (map prefix-tree/add-satisfies-constraints? routes))]
    (reduce (fn [tree [path related-routes]]
              (assoc tree path (prefix-tree/create-payload-fn related-routes)))
            {}
            initial-tree-map)))

(defn router
  "Given a sequence of routes, return a router which satisfies the
  io.pedestal.http.route.router/Router protocol."
  [routes]
  (if (some prefix-tree/contains-wilds? (map :path routes))
    (prefix-tree/router routes)
    (->MapRouter routes (matching-route-map routes))))


(defn execute-interceptors
  [hellhound-context interceptors req]
  (let [context {:input  (:input hellhound-context)
                 :output (:output hellhound-context)
                 :request req}]
    (:response (io.pedestal.interceptor.chain/execute context interceptors))))

(defn not-found
  [req]
  {:status 404 :headers {} :body "NOT FOUND!"})

(defn route-handler
  [hellhound-context routes]
  (fn [req]
    (let [matched      (io.pedestal.http.route.router/find-route routes req)
          interceptors (:interceptors matched)]

      (if matched
        (execute-interceptors hellhound-context interceptors req)
        (not-found req)))))

(defn hello
  [req]
  {:status 200
   :headers []
   :body "Welcome to HellHound"})

(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})


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
  [input output]
  (fn
    [req]
    (log/info "Accpting WS connection")
    (->
     (deferred/let-flow [socket (http/websocket-connection req)
                         router-stream (stream/stream 100)
                         output-stream (stream/stream 100)]
       (stream/connect socket output))
     (deferred/catch
         (fn [err]
           (log/error err)
           non-websocket-request)))))

(defn create-routes
  [routes input-stream output-stream])
