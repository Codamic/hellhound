(ns hellhound.http.interceptors.request
  (:require
   [io.pedestal.interceptor :as interceptor]
   [hellhound.logger.formatters :as f]
   [hellhound.logger :as log]))


(defn- cstr
  "Returns a colorful `v` if `colorful?` is true or v
  otherwise."
  [colorful? style color v]
  (if colorful?
    (f/color-msg-with-style style color v)
    v))


(defn- request-logger
  [colorful?]
  (fn [ctx]
    (let [req (:request ctx)
          params (:params req)]
      (log/trace req)
      (log/info (format "[%s] %s: %s , PARAMS: %s"
                        (cstr colorful? :bold :green "REQ")
                        (.toUpperCase (name (:request-method req)))
                        (:path-info req)
                        params))
      ctx)))


(defn- response-logger
  [colorful?]
  (fn [ctx]
    (let [req (:request ctx)
          res (:response ctx)
          status (:status res)]
      (log/info (format "[%s] Status: %s, %s: %s"
                        (cstr colorful? :bold :purple "RES")
                        status
                        (.toUpperCase (name (:request-method req)))
                        (:path-info req)))
      ctx)))


(defn- error-logger
  [colorful?]
  (fn [ctx]
    (log/error "FIXME")
    ctx))


(defn logger
  "An intercaptor to log the request and response."
  [config]
  (let [colorful? (or (:colorful-logs config) true)]
    (interceptor/interceptor
     {:name ::logger
      :enter (request-logger colorful?)
      :leave (response-logger colorful?)
      :error (error-logger colorful?)})))
