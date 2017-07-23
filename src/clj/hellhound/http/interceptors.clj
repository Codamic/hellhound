(ns hellhound.http.interceptors
  "Bunch of interceptors to be used in `development` and `production`
  service maps."
  (:require
   [io.pedestal.http   :as http]
   [io.aviso.exception :as excep]
   [hellhound.logger   :as logger]))

(defn- error-handler-on-error
  [ctx err]
  (logger/exception err)
  (println (excep/format-exception err)))



(def error-handler-interceptor
  "An interceptor to catch any exception in interceptor chain
  and handle them base on the configuration or throw them at
  your face."
  {:name ::error-handler-interceptor
   :error error-handler-on-error})


(defn remove-unnecessary-interpretors
  [service-map]
  (let [interceptors (::http/interceptors service-map)]
    (assoc service-map ::http/interceptors (rest interceptors))))

(defn hellhound-interceptors
  [service-map]
  (assoc service-map ::http/interceptors
             (-> []
                 (conj error-handler-interceptor)
                 (concat (::http/interceptors service-map)))))

(defn default
  "Injects a vector of pre configured hellhound interceptors
  to the given `service-map`."
  [service-map]
  (-> service-map
      (http/default-interceptors)
      (remove-unnecessary-interpretors)
      (hellhound-interceptors)))
