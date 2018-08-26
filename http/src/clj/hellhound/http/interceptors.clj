(ns hellhound.http.interceptors
  (:require
   [io.pedestal.http.route :as route]
   [io.pedestal.http.cors :as cors]
   [io.pedestal.http :as http]
   [io.pedestal.http.csrf :as csrf]
   [io.pedestal.http.secure-headers :as sec-headers]
   [io.pedestal.http.ring-middlewares :as middlewares]))

(defn default
  [& interceptors]
  (apply merge [http/log-request
                cors/allow-origin
                http/not-found
                middlewares/session
                csrf/anti-forgery
                route/query-params
                route/method-param
                sec-headers/secure-headers]
         interceptors))

(defn dev
  [ctx]
  (http/dev-interceptors ctx))
