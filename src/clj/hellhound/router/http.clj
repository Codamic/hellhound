(ns hellhound.router.http
  (:require
   [io.pedestal.http.route.definition.table :as table]
   [hellhound.components.core               :as system]
   [hellhound.core                          :as hellhound]))


(defn- websocket
  []
  (system/get-component :websocket))


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

(defmacro defroutes
  [name & body]
  (let [config (hellhound/application-config)
        host   (or (:host config) "localhost")
        scheme (or (:scheme config) "http")]
    `(def ~name (io.pedestal.http.route/expand-routes
                   #{{:host ~host :scheme ~scheme}
                     ["/hellhound" :get  hellhound.router.http.ws-handshake  :as :hellhound-ws-handshake]
                     ["/hellhound" :post hellhound.router.http.ajax-ws-post  :as :hellhound-ws]
                     ~body}))))
