(ns hellhound.system
  "Systems are the most important thing in the **HellHound** ecosystem.
  Systems define how your application should work."
  ^{:author "Sameer Rahmani (@lxsameer)"}
  (:require [hellhound.config              :as config]
            [hellhound.system.core         :as core]
            [hellhound.config.defaults     :as default]))

(defn set-system!
  "Set the default system"
  [system]
  (core/set-system! system))

(defn system
  []
  (core/get-system))

(defn start-system
  "Start the given system. RunsCreate an instance of all the components by running
  the given `factory` functions. Then starts all the components by calling
  `start` function of the instances."
  [system]
  ;; Read the configuration for the current runtime environment which
  ;; specified by `HH_ENV` environment. Default env is `:development`
  (config/load-runtime-configuration))

(defn start!
  []
  (core/start-system! @core/system))

(defn stop!
  []
  (core/stop-system! @core/system))


(defmacro defsystem
  "Define a system map according to clojure component defination."
  [system-name & body]
  `(def ~system-name
     (merge hellhound.components.defaults/system-structure
            {:components (merge ~@body)})))
