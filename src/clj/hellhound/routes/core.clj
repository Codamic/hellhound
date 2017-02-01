(ns hellhound.routes.core
  "This namespace contains functions and macros to define
  the application routes usigin the `bidi` library. Hellhound's
  policy is to wrap the internal library interface to in order to
  create a unified interface. But most of the concepts of the internal
  library remains untouched. So for more info checkout the `bidi`
  [documents](https://github.com/juxt/bidi)."
  (:require [bidi.bidi        :refer [match-route path-for] :as bidi]
            [hellhound.system :refer [get-system]]
            [bidi.ring        :as biring]
            [clojure.pprint   :refer [pprint]]
            [taoensso.timbre  :as log]))


(def __routes__ (atom nil))

(defn- websocket
  []
  (:websocket (get-system)))

(defn route-table
  "Return the current route table. Use this function for debuggin purposes."
  []

   @__routes__)

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


(defn hellhound-routes
  []
  ["hellhound"
   {:get  {"" (fn [req]  ((:ring-ajax-get-or-ws-handshake (websocket)) req))}
    :post {"" (fn [req]  ((:ring-ajax-post (websocket)) req))}}])


(defn make-handler
  "Create a ring handler from the given bidi route data."
  [all-routes
   ]
  (let [;;all-routes (concat (first routes) (concat (second routes) (hellhound-routes)))
        ]
    (reset! __routes__ all-routes)
    (biring/make-handler all-routes)))

(defn GET
  [route handler]
  [route {:get handler}])

(defn redirect-to-not-found
  []
  [true not-found])
