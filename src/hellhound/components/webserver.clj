(ns hellhound.components.webserver
  "Hellhound webserver component namespace. Webserver component is
  responsible for running an `Immutant` webserver and setting up a
  `bidi` ring handler. In order to run a webserver use `webserver`
  function  alongside with `hellhound.system.defsystem` macro you can define a
  system with webserver component in it. But if you want to define your
  own system map you can use `make-webserver` function and do it like
  you used to do."
  (:require [ring.middleware.defaults   :refer [site-defaults]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre           :as log]
            [environ.core               :refer [env]]
            [immutant.web               :as web]))



(defn- http-port
  []
  (or (Integer. (env :http-port)) 4000))

(defn- http-host
  []
  (or (env :http-host) "0.0.0.0"))


(defrecord Webserver [handler host port]
  component/Lifecycle

  (start [component]
    (if-not (:server component)
      (let [config {:host host :port port :path "/"}
            server (do (-> (str "Starting web server. Listening on host: %s "
                                "and port: %d")
                           (format host port)
                           (log/info))
                       (web/run handler config))]
        (assoc component
               :server server
               :host   host
               :port   port))
      component))

  (stop [component]
    (if-let [server (:server component)]
      (do (-> (str "Stopping web server on host: %s and port: %d")
              (format (:host component) (:port component))
              (println))
          (web/stop server)
          (dissoc component :server))
      component)))




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
  "Creat and launch an `immutant` server."
  ([routes]
   (make-webserver routes 4000 {}))

  ([routes host]
   (make-webserver routes host 4000 {}))

  ([routes host port]
   (make-webserver routes host port {}))

  ([routes host port handler-options]
   (->Webserver (ring-handler routes handler-options) host port)))

;;

(defn webserver
  "Create a webserver instance from the webserver components.
  This function is meant to be used with `hellhound.system.defsystem` macro."
  ([system-map routes]
   (webserver system-map routes {}))

  ([system-map routes handler-options]
   (assoc-in system-map [:webserver]
             (make-webserver routes (http-host) (http-port) handler-options))))
