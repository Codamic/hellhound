(ns hellhound.http.interceptors
  (:require
   [io.pedestal.http.route :as route]
   [io.pedestal.http.cors :as cors]
   [io.pedestal.http :as http]
   [io.pedestal.http.csrf :as csrf]
   [io.pedestal.http.secure-headers :as sec-headers]
   [io.pedestal.http.ring-middlewares :as middlewares]
   [hellhound.logger :as log]
   [hellhound.http.interceptors.request :as request]))

(defn uri->path-info
  [config]
  (io.pedestal.interceptor/interceptor
   {:name ::uri->path-info
    :enter (fn [ctx]
             (let [req (:request ctx)]
               (assoc ctx
                      :request
                      (assoc req :path-info (:uri req)))))}))

(defn resource-middleware
  [config]
  (middlewares/resource (or (:public-path config) "public")))


(defn query-params
  [config]
  route/query-params)


(defn method-param
  [config]
  (route/method-param))


(defn allow-origin
  [config]
  (let [host (:host config)
        port (:port config)
        cors-hosts (or (:cors-hosts config)
                       [])]

    (cors/allow-origin (concat [(str host ":" port)]
                               cors-hosts))))


(defn session
  [config]
  (middlewares/session))


(defn anti-forgery
  [config]
  (csrf/anti-forgery))


(defn content-type
  [config]
  (middlewares/content-type))


(defn secure-headers
  [config]
  (sec-headers/secure-headers {:content-security-policy-settings
                               {:object-src "none"}}))


(defn not-found
  [config]
  io.pedestal.http/not-found)


(defn default-chain
  [config & interceptors]
  (concat [(uri->path-info config)
           (query-params config)
           (method-param config)
           (request/logger config)
           (allow-origin config)
           (session config)
           (anti-forgery config)
           (content-type config)
           (secure-headers config)]
          interceptors
          [(resource-middleware config)
           (not-found config)]))
