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
   [hellhound.core     :as hellhound]))

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
  [routes config]
  (fn [this context]
    (assoc this :instance (http/start-server routes config))))

(defn stop!
  "Stops the running webserver server."
  [this]
  (if (:instance this)
    (do
      (.close (:instance this))
      (dissoc this :instance))
    this))


(defn factory
  "Returns a new webserver component by the given `routes` and an
  optional `config` map.

  The `routes` argument should be a valid **HellHound** route collection,
  compatible with [bidi library](https://github.com/juxt/bidi). **Hellhound**
  provides a helper namespace for dealing with routes. Checkout
  [[hellhound.http]] and [[hellhound.http.route]] namespaces for more info."
  ([routes]
   (factory routes (hellhound/get-config :http)))
  ([routes config]
   (spec/validate ::aleph-config config "Aleph configuration is invalid.")
   {:hellhound.component/name ::aleph
    :hellhound.component/start-fn (start! routes config)
    :hellhound.component/stop-fn stop!}))
