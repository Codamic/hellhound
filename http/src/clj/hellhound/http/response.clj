(ns hellhound.http.response
  (:require
   [hellhound.streams :as streams]
   [hellhound.async :as async]))


(defn async-response
  ([]
   (async-response {}))
  ([config]
   (async/timeout! (async/deferred)
                   (or (:response-timeout config)
                       5000)
                   {:status 502 :body "Timeout"})))


(defn resolver
  [v]
  (let [response (:response v)
        deferred (:response-deferred v)]
    (when (and response deferred)
      (async/success! deferred response))))
