(ns hellhound.system
  "Systems are the most important thing in the **HellHound** ecosystem.
  Systems define how your application should work."
  ^{:author "Sameer Rahmani (@lxsameer)"}
  (:require [hellhound.components.core     :as component]
            [hellhound.config              :as config]
            [hellhound.system.core         :as core]
            [hellhound.config.defaults     :as default]))

(defn set-system!
  "Set the default system"
  [system]
  (core/reset-system! system))

(defn system
  []
  (core/get-system))

(defn get-component
  [component-name]
  (:instance (core/get-system-entry component-name)))

(defn start-system
  "Start the given system. RunsCreate an instance of all the components by running
  the given `factory` functions. Then starts all the components by calling
  `start` function of the instances."
  [system]
  ;; Read the configuration for the current runtime environment which
  ;; specified by `HH_ENV` environment. Default env is `:development`
  (config/load-runtime-configuration)
  (component/instantiate-components system))


;; TODO: Wait for all the components to terminate gracefully
;; TODO: Add `force` flag.
(defn stop-system
  "Stop the given system by calling stop on all components"
  [system]
  (component/iterate-components system component/stop-component))

(defn start
  "Start the default system"
  []
  (start-system (system)))

(defn stop
  "Stop the default system"
  []
  (stop-system (system)))

(defn restart
  "Restart the default system"
  []
  (stop)
  (start))

(defmacro defsystem
  "Define a system map according to clojure component defination."
  [system-name & body]
  `(def ~system-name
     (merge hellhound.components.defaults/system-structure
            {:components (merge ~@body)})))
