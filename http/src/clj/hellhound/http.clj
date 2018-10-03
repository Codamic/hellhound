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

(def ws-interceptors (ws/interceptor-factory))

(def default-router-configuration
  {:port 3000
   :host "localhost"
   :scheme "http"
   :websocket-endpoint "/ws"})


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
(defn default-routes
  ([]
   (default-routes default-router-configuration))
  ([config]
   (route/router
    (route/expand-routes
     #{{:host   (:host config)
        :scheme (:scheme config)
        :port   (:port config)}
       ["/" :get handlers/default-handler :route-name :home]
       [(:websocket-endpoint config)
        :get
        (ws/interceptor-factory)]}))))


;; hellhound.http.websocket shortcuts ----------------------

(defmacro defrouter
  [name & routes]
  `(defn ~name
     ([]
      (~name hellhound.http/default-router-configuration))
     ([config#]
      (hellhound.http.route/expand-routes
       #{{:host   (:host config#)
          :scheme  (:scheme config#)
          :port    (:port config#)}
         ~@routes}))))
