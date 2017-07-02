(ns hellhound.system
  (:require [hellhound.components.core     :as component]
            [hellhound.config              :as config]
            [hellhound.components.defaults :as defaults]
            [hellhound.config.defaults :as default]))

(defn set-system!
  "Set the default system"
  [system]
  (swap! defaults/system (fn [_] system)))

(defn system
  []
  @defaults/system)

(defn get-system-entry
  [component-name]
  (get (:components (system)) component-name))

(defn get-component
  [component-name]
  (:instance (get-system-entry component-name)))

(defn start-system
  "Start the given system and call start on all the components"
  [system]
  ;; Read the configuration for the current runtime environment which
  ;; specified by `HH_ENV` environment. Default env is `:development`
  (config/load-runtime-configuration)
  (component/iterate-components system component/start-component))

;; TODO: Wait for all the components to terminate gracefully
;; TODO: Add `force` flag.
(defn stop-system
  "Stop the given system by calling stop on all components"
  [system]
  (component/iterate-components system component/stop-component))

(defn start
  "Start the default system"
  []
  (start-system defaults/system))

(defn stop
  "Stop the default system"
  []
  (stop-system defaults/system))

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
            {:components ~@body})))