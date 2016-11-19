(ns hellhound.components.webserver
  (:require [ring.middleware.defaults :refer [site-defaults]]
            (system.components
             [http-kit :refer [new-web-server]])))


(defn ring-handler
  "Create a ring handler from the given routs. This function
  will make sure that sente's required middlewares are present
  in the resulted handler"
  [routes]
  (let [ring-defaults-config
        (-> site-defaults
            (assoc-in [:static :resources] "/")
            (assoc-in [:security :anti-forgery]
                      {:read-token (fn [req] (-> req :params :csrf-token))}))]

    ;; To make sure that sente's required middlewares are present.
    (ring.middleware.defaults/wrap-defaults routes ring-defaults-config)))

(defn make-webserver
  "Creat and launch an `http-kit` server."
  ([routes]
   (new-web-server 4000 (ring-handler routes))))


(defn webserver
  [system-map routes]
  (assoc-in system-map [:webserver] (make-webserver routes)))
