(ns hellhound.system
  (:require [hellhound.component.core :as component]
            [hellhound.core :as hellhound]))

(defn start-system
  "Start the given system and call start on all the components"
  [system]
  ;; Read the configuration for the current runtime environment which
  ;; specified by `HH_ENV` environment. Default env is `:development`
  (hellhound/load-runtime-configuration)
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
  (start-system component/default-system))

(defn stop
  "Stop the default system"
  []
  (stop-system component/default-system))

(defn restart
  "Restart the default system"
  []
  (stop)
  (start))

(defmacro defsystem
  "Define a system map according to clojure component defination."
  [system-name & body]
  `(defn ~system-name []
     (-> hellhound.component.core/default-system-structure
         ~@body)))

(defmacro with-hellhound-system
  "Define a system map with some pre defined components."
  [system-name & body]
  `(-> hellhound.core/default-system-structure
       ;; TODO: Pass the options from config file
       (hellhound.component.websocket/make-websocket-component {})
       (hellhound.component.pedestal/make-pedestal-component {})
       ~@body))
