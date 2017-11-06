(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha :as s]
   [io.pedestal.http.route.prefix-tree :as prefix-tree]
   [io.pedestal.http.route.router :as pedestal-router]
   [io.pedestal.http.route        :as pedestal-route]
   [hellhound.logger   :as log]
   [hellhound.component :as hcomp]
   [hellhound.core     :as hellhound]
   [hellhound.http.websocket.json :as jpack]
   [hellhound.http.websocket.core :as packer]
   [hellhound.http.handlers :as handlers]))


(def expand-routes pedestal-route/expand-routes)

(defrecord MapRouter [routes tree-map]
  pedestal-router/Router
  (find-route [this req]
    ;; find a result in the prefix-tree - payload could contains mutiple routes
    (when-let [match-fn (tree-map (:uri req))]
      ;; call payload function to find specific match based on method, host, scheme and port
      (when-let [route (match-fn req)]
        ;; return a match only if query constraints are satisfied
        ;; the `nil` here is "path-params"
        (when ((::prefix-tree/satisfies-constraints? route) req nil)
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
  "Executes the given `intercetors` chain by creating a new `context` map
  from the `hellhound-context` and passing it to each intercaptor.

  **Pedestal routes** library returns a map for the matched route. In that
  map **Pedestal** provides a key called `:interceptors` which its value
  is a chain (collection) of intercaptors assigned to the `route` in question.
  In order to execute the chain we used the
 `io.pedestal.interceptor.chain/execute` function and pass it a new context
  map for each interceptor. The `context` contains `input` and `output` of the
  webserver component along side with the incoming reqeust map."
  [hellhound-context interceptors req]
  (let [context (assoc hellhound-context :request req)]
    (:response (io.pedestal.interceptor.chain/execute context interceptors))))


(defn route-handler
  "The default ring handler to be used with a ring webserver (webserver
  component) which is responsible for resolving routes.

  This handler is responsible for resolving routes with the incomming
  request and executing the chain of interceptors. It creates a context map
  and pass it to each interceptor."
  [hellhound-context routes]
  (fn [req]
    (let [matched      (io.pedestal.http.route.router/find-route routes req)
          interceptors (:interceptors matched)]

      (if matched
        (execute-interceptors hellhound-context interceptors req)
        (handlers/not-found req)))))
