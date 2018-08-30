(ns hellhound.http.interceptors
  (:require
   [io.pedestal.http.route :as route]
   [io.pedestal.http.cors :as cors]
   [io.pedestal.http :as http]
   [io.pedestal.http.csrf :as csrf]
   [io.pedestal.http.secure-headers :as sec-headers]
   [io.pedestal.http.ring-middlewares :as middlewares]))

(defn default-chain
  [& interceptors]
  (concat [http/log-request
           (cors/allow-origin "localhost:3000")

           (middlewares/session)
           (csrf/anti-forgery)
           (middlewares/content-type)
           route/query-params
           (route/method-param)
           (sec-headers/secure-headers)]
          interceptors
          [(middlewares/fast-resource "public" {:index? true})
           io.pedestal.http/not-found]))



(defn dev
  [ctx]
  (http/dev-interceptors ctx))


(defn merge-interceptors
  [& interceptor-colls]
  (distinct (flatten interceptor-colls)))
