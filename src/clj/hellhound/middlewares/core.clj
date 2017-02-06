(ns hellhound.middlewares.core
  "This namespace contains sets of middleware kits for different
environments like `development`, `test` and `production`"
  {:author "Sameer Rahmani <lxsameer@gnu.org>"}
  (:require [ring.middleware.session        :refer [wrap-session]]
            [ring.middleware.resource       :refer [wrap-resource]]
            [ring.middleware.content-type   :refer [wrap-content-type]]
            [ring.middleware.not-modified   :refer [wrap-not-modified]]
            [ring.middleware.params         :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.reload         :refer [wrap-reload]]
            [ring.middleware.anti-forgery   :refer [wrap-anti-forgery]]
            [immutant.web.middleware        :refer [wrap-development wrap-write-error-handling]]
            [hellhound.middlewares.logger   :refer [wrap-logger]]))


(defn wrap-development-kit
  "A short cut function to wrap the useful middlewares
   for development environment around the given handler."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      wrap-logger
      wrap-anti-forgery
      wrap-session
      (wrap-resource "assets")
      wrap-content-type
      wrap-not-modified
      wrap-reload))

(defn wrap-testing-kit
  "A short cut function to wrap the useful middlewares
   for testing environment around the given handler."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      wrap-logger
      wrap-anti-forgery
      wrap-session
      (wrap-resource "assets")
      wrap-content-type
      wrap-not-modified
      wrap-reload))

(defn wrap-production-kit
  "A short cut function to wrap the useful middlewares
   for production around the given handler."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      wrap-logger
      wrap-anti-forgery
      wrap-session
      (wrap-resource "assets")
      wrap-content-type
      wrap-not-modified))
