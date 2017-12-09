(ns hellhound.components.webserver
  "The default web/websocket server component of **HellHound**.

  This component provides a webserver based on [aleph](//aleph.io).
  In order this use this component simply call the `factory` function.
  It's going to get the configuration from the environment edn file and
  returns a component map."
  ^{:author "Sameer Rahmani (@lxsameer)"}
  (:require
   [clojure.spec.alpha :as s]
   [aleph.http         :as http]
   [hellhound.logger   :as log]
   [hellhound.spec     :as spec]
   [hellhound.core     :as hellhound]
   [hellhound.component :as hcomp]
   [hellhound.http.route :as router]))

;; TODO: Extract the spec check into a predicate function called map-with
(s/def ::aleph-config
  (s/and map?
         #(and (contains? % :host)
               (string? (:host %)))
         #(and (contains? % :port)
               (int? (:port %)))))


(defn start!
  "Returns a start function for the webserver component.

  The return function starts the webserver using the given `routes`
  and `config` map.

  NOTE: This function RETURNS a start function."
  [routes hooks config]
  (fn [this context]
    (let [new-context (assoc context
                             :hooks  hooks
                             :input  (hcomp/input this)
                             :output (hcomp/output this))
          http-routes (router/route-handler new-context routes)]
      (assoc this
             :instance
             (http/start-server http-routes config)))))


(defn stop!
  "Stops the running webserver server."
  [this]
  (if (:instance this)
    (do
      (.close (:instance this))
      (dissoc this :instance))
    this))


(def default-hooks
  {:send->user? (fn [x] true)})


(defn factory
  "Returns a new webserver component by the given `routes` and an
  optional `config` map.

  The `routes` argument should be a valid **HellHound** route collection,
  compatible with [bidi library](https://github.com/juxt/bidi). **Hellhound**
  provides a helper namespace for dealing with routes. Checkout
  [[hellhound.http]] and [[hellhound.http.route]] namespaces for more info."
  ([routes]
   (factory routes default-hooks (hellhound/get-config :http)))

  ([routes hooks]
   (factory routes
            (merge default-hooks hooks)
            (hellhound/get-config :http)))

  ([routes hooks config]
   (spec/validate ::aleph-config config "Aleph configuration is invalid.")
   (let [web-hooks (merge default-hooks
                          hooks)]
     {:hellhound.component/name ::webserver
      :hellhound.component/start-fn (start! routes web-hooks config)
      :hellhound.component/stop-fn stop!})))
