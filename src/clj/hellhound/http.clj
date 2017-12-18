(ns hellhound.http
  "This namespace contains several shortcuts and helpers
  to work with HellHound's HTTP features such as router,
  websocket, etc."
  (:require
   [hellhound.core           :as hh]
   [hellhound.http.route     :as route]
   [hellhound.http.websocket :as ws]
   [hellhound.http.handlers  :as handlers]))

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
