(ns hellhound.components.webserver
  "The default web/websocket server component of **HellHound**.

  This component provides a webserver based on [aleph](//aleph.io).
  In order this use this component simply call the `factory` function.
  It's going to get the configuration from the environment edn file and
  returns a component map."
  {:author "Sameer Rahmani (@lxsameer)"}
  (:require
   [clojure.spec.alpha   :as s]
   [aleph.http           :as aleph]
   [hellhound.logger     :as log]
   [hellhound.core       :as hellhound]
   [hellhound.specs       :as spec]
   [hellhound.component  :as hcomp]
   [hellhound.http       :as http]
   [hellhound.streams    :as streams]
   [hellhound.http.handlers :as handlers]
   [hellhound.http.response :as response]
   [hellhound.http.route :as router]))

;; TODO: Extract the spec check into a predicate function called map-with
(s/def ::aleph-config
  (s/and map?
         #(and (contains? % :host)
               (string? (:host %)))
         #(and (contains? % :port)
               (int? (:port %)))))


(def CONFIG_ERR_MSG
  "Aleph configuration is invalid. Probably forgot to add the configuration to your system.")

(defn start!
  "Returns a start function for the webserver component.

  The return function starts the webserver using the given `routes`
  and `config` map.

  NOTE: This function RETURNS a start function."
  [config]
  (fn [this context]
    (let [new-context (assoc context
                             :input  (hcomp/input this)
                             :output (hcomp/output this))
          config      (merge (:config new-context) config)]

      (spec/validate ::aleph-config config CONFIG_ERR_MSG)
      (assoc this
             :instance (aleph/start-server (handlers/req-to-stream new-context)
                                           config)
             :config   config))))


(defn stop!
  "Stops the running webserver server."
  [this]
  (when-let [server (:instance this)]
    (.close server))
  (dissoc this :instance))

(defn factory
  "Returns a new webserver component by the given `routes` and an
  optional `config` map.

  The `routes` argument should be a valid **HellHound** route collection,
  compatible with Pedestal library. **Hellhound**
  provides a helper namespace for dealing with routes. Checkout
  [[hellhound.http]] and [[hellhound.http.route]] namespaces for more info."
  ([]
   (factory {}))

  ([config]
   {:hellhound.component/name ::webserver
    :hellhound.component/start-fn (start! config)
    :hellhound.component/stop-fn stop!
    :hellhound.component/fn
    (fn [component]
      (streams/consume response/resolver
                       (hcomp/input component)))}))
