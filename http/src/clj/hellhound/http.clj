(ns hellhound.http
  "This namespace contains several shortcuts and helpers
  to work with HellHound's HTTP features such as router,
  websocket, etc."
  (:require
   [hellhound.core           :as hh]
   [hellhound.http.route     :as route]
   [hellhound.http.websocket :as ws]
   [hellhound.http.handlers  :as handlers]
   [hellhound.http.interceptors :as interceptors]))

;; hellhound.http.route shortcuts --------------------------
(defn router
  [routes]
  (route/router routes))

(defn expand-routes
 "Produces and returns a sequence of route-maps from the given `route-spec`
  (the route specification).

  The return value is a verbose form of route-maps which is understandable by
  Pedestal router."
  [route-spec]
  (route/expand-routes route-spec))

;; The default routes for a hellhound application.
(def default-routes
  (route/router
   (route/expand-routes
    #{{:host   (hh/get-config :http :host)
       :scheme (hh/get-config :http :scheme)
       :port   (hh/get-config :http :port)}
      ["/" :get handlers/default-handler :route-name :home]
      [(hh/get-config :http :websocket-endpoint)
       :get
       (ws/interceptor-factory)]})))

;; hellhound.http.websocket shortcuts ----------------------
(def ws-interceptors (ws/interceptor-factory))

(defmacro defrouter
  [name & routes]
  `(def ~name
     (hellhound.http.route/router
      (hellhound.http.route/expand-routes
       #{{:host   (hellhound.core/get-config :http :host)
          :scheme  (hellhound.core/get-config :http :scheme)
          :port    (hellhound.core/get-config :http :port)}
         ~@routes}))))
