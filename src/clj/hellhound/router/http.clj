(ns hellhound.router.http
  (:require
   [io.pedestal.http.route.definition.table :as table]
   [hellhound.components.core               :as system]))


(defn- websocket
  []
  (system/get-component :websocket))

(defn route-table
  "Return the current route table. Use this function for debuggin purposes."
  [

   @__routes__])

(defn not-found
  "Default route for bidi. This means that a route that does not
  exists in the router will cause a 404 error."
  [request]
  ;; TODO: We should provide a better view of routes here
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (str "<pre>"
              (clojure.string/replace (with-out-str
                                        (pprint (route-table)))
                                      "\n" "<br />")
              "</pre>")})

(defn ws-handshake
  [context]
  (let [ajax-ws-handshake (:ring-ajax-get-or-ws-handshake (websocket))])
  (ajax-ws-handshake req))

(defn ajax-ws-post
  [context]
  (let [post-fn (:ring-ajax-post (websocket))]
    (post-fn req)))

(defmacro defroutes
  [name & body]
  (let [config (hellhound/application-config)
        host   (or (:host config) "localhost")
        scheme (or (:scheme config) "http")]
   `(table/expand-routes
     #{{:host ~host :scheme ~scheme}
       ["/hellhound" :get  ~ws-handshake  :as :hellhound-ws-handshake]
       ["/hellhound" :post ~ajax-ws-post  :as :hellhound-ws]
       ~@body})))
