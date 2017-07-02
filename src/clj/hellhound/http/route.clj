(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha                      :as spec]
   [io.pedestal.http.route                  :as route]
   [hellhound.components.core               :as system]
   [hellhound.core                          :as hellhound]))

;; Specs ---------------------------------------------------
(spec/def ::vector-of-routes (spec/coll-of vector?))
(spec/def ::user-routes (spec/and set? ::vector-of-routes))

;; Private Functions ---------------------------------------
(defn- websocket
  []
  (system/get-component :websocket))


;; Public Functions ----------------------------------------
(defn not-found
  "Default route for bidi. This means that a route that does not
  exists in the router will cause a 404 error."
  [request]
  ;; TODO: We should provide a better view of routes here
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body ""})


(defn ws-handshake
  [context]
  (let [ajax-ws-handshake (:ring-ajax-get-or-ws-handshake (websocket))]
    (ajax-ws-handshake context)))

(defn ajax-ws-post
  [context]
  (let [post-fn (:ring-ajax-post (websocket))]
    (post-fn context)))


(defn hellhound-routes
  "DOCTODO"
  []
  (let [host   (hellhound/get-config :host)
        scheme (hellhound/get-config :scheme)]
    #{["/hellhound" :get  [ws-handshake]  :route-name :hellhoud/ws-handshake]
      ["/hellhound" :post [ajax-ws-post]  :route-name :hellhound/ws]}))


(defn expand-routes
  "Retruns a set of user defined routes and injects hellhounds
  routes into the set.

  DOCTODO"
  [routes]
  (let [new-routes (clojure.set/union
                    (hellhound-routes)
                    (spec/conform ::user-routes routes))]
    (route/expand-routes
     new-routes)))

(defmacro defroutes
  "A shortcut macro for defining routes.
  DOCTODO"
  [routes-name routes]
  `(def ~routes-name
     (hellhound.http.route/expand-routes #{~@routes})))