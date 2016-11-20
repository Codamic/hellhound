(ns hellhound.components.webserver
  "Hellhound webserver component namespace. Webserver component is
  responsible for running a `http-kit` webserver and setting up a
  `compojure` ring handler. In order to run a webserver use `webserver`
  function  alongside with `hellhound.system.defsystem` macro you can define a
  system with webserver component in it. But if you want to define your
  own system map you can use `make-webserver` function and do it like
  you used to do."
  (:require [ring.middleware.defaults :refer [site-defaults]]
            [environ.core :refer [env]]
            (system.components
             [http-kit :refer [new-web-server]])))



(defn- http-port
  []
  (Integer. (env :http-port)))

(defn ring-handler
  "Create a ring handler from the given routs. This function
  will make sure that sente's required middlewares are present
  in the resulted handler"
  [routes options]
  (let [defaults-config
        (-> site-defaults
            (assoc-in [:static :resources] "/")
            (assoc-in [:security :anti-forgery]
                      {:read-token (fn [req] (-> req :params :csrf-token))}))
        ring-defaults-config (merge defaults-config options)]

    ;; To make sure that sente's required middlewares are present.
    (ring.middleware.defaults/wrap-defaults routes ring-defaults-config)))


(defn make-webserver
  "Creat and launch an `http-kit` server."
  ([routes]
   (make-webserver 4000 {}))
  ([routes port]
   (make-webserver port {}))
  ([routes port handler-options]
   (new-web-server port (ring-handler routes handler-options))))


(defn webserver
  "Create a webserver instance from the webserver components.
  This function is meant to be used with `hellhound.system.defsystem` macro."
  ([system-map routes]
   (webserver system-map routes {}))

  ([system-map routes handler-options]
   (assoc-in system-map [:webserver]
             (make-webserver routes (http-port) handler-options))))
