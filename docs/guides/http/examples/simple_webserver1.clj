(ns components.webserver-example1
  (:require
   [hellhound.system :as system]
   [hellhound.component :as hcomp]
   [hellhound.components.webserver :as web] <1>
   [hellhound.components.transform :as transform] <2>
   [hellhound.http :as http]
   [manifold.stream :as s]))

;; System definition.
(def system
  {:components
   [(web/factory http/default-routes) <3>
    (transform/factory ::output <4>
                       (fn [context msg]
                         (println "RECEIVED: " msg)
                         msg))]

   :workflow [[::web/webserver  ::output]
              [::output ::web/webserver]]}) <5>

(defn main
  []
  (system/set-system! system)
  (system/start!))
