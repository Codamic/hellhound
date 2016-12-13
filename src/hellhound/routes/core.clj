(ns hellhound.routes.core
  "This namespace contains functions and macros to define
  the application routes usigin the `bidi` library. Hellhound's
  policy is to wrap the internal library interface to in order to
  create a unified interface. But most of the concepts of the internal
  library remains untouched. So for more info checkout the `bidi`
  [documents](https://github.com/juxt/bidi)."
  (:require [bidi.bidi        :refer [match-route path-for] :as bidi]
            [hellhound.system :refer [get-system]]
            [bidi.ring        :as biring]))


(def __routes__ (atom nil))

(defn- websocket
  []
  (:websocket (get-system)))


(defn hellhound-routes
  []
  {"hellhound"
   {:get  {"" (fn [req] ((:ring-ajax-get-or-ws-handshake (websocket)) req))}
    :post {"" (fn [req] ((:ring-ajax-post (websocket)) req))}}})


(defn make-handler
  "Create a ring handler from the given bidi route data."
  [routes]
  (let [all-routes (assoc routes 1 (merge (second routes) (hellhound-routes)))]
    (reset! __routes__ all-routes)
    (biring/make-handler all-routes)))



(defn route-table
  "Return the current route table. Use this function for debuggin purposes."
  []
  @__routes__)

(defn make-route
  "Create data sctructure that represents a `bidi` route."
  ([method url func]
   (make-route method url func {}))

  ([method url func options]
   (let [{:keys [tag]} options
         handler (if (nil? tag)
                   func
                   (bidi/tag func tag))]
     {method [url handler]})))

(defn get_
  "A shortcut function to create a route which is responsible
  a get request to the given url."
  [url func]
  (make-route :get url func))

(def POST
  "A shortcut function to create a route which is responsible
  a get request to the given url."
  (partial make-route :post))

(def HEAD
  "A shortcut function to create a route which is responsible
  a get request to the given url."
  (partial make-route :head))

(def DELETE
  "A shortcut function to create a route which is responsible
  a get request to the given url."
  (partial make-route :delete))

(def PUT
  "A shortcut function to create a route which is responsible
  a get request to the given url."
  (partial make-route :put))

(def PATCH
  "A shortcut function to create a route which is responsible
  a get request to the given url."
  (partial make-route :patch))


(defn defroutes
  [name & body]
  (let [res (merge body [true (fn [req]  {:status 404
                                          :headers {"Content-Type" "text/plain"}
                                          :body "nnnn"})])]
    (make-handler body)))
