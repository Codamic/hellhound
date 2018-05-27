(ns hellhound.system
  "Systems are the most important thing in the **HellHound** ecosystem.
  Systems define how your application should work."
  ^{:author "Sameer Rahmani (@lxsameer)"}
  (:require [hellhound.config              :as config]
            [hellhound.system.core         :as core]
            [hellhound.system.workflow     :as workflow]
            [hellhound.logger              :as logger]
            [hellhound.system.protocols    :as impl]
            [hellhound.config.defaults     :as default]))

(defn set-system!
  "Sets the default system of HellHound application to the given
  `system` map."
  {:added      1.0
   :public-api true}
  [system-map]
  (core/set-system! system-map))

(defn system
  "Returns the processed system."
  {:added      1.0
   :public-api true}
  []
  (core/get-system))

(defn start!
  "Starts the default system by calling start on all the components.

  TODO: more doc"
  {:added      1.0
   :public-api true}
  []
  ;; Read the configuration for the current runtime environment which
  ;; specified by `HH_ENV` environment. Default env is `:development`
  (config/load-runtime-configuration)
  (logger/init! (config/get-config :logger))
  (core/set-system!
   (-> @core/system
       (core/init-system)
       (core/start-system)
       (workflow/setup)))

  (logger/info "System has been started successfully."))

(defn stop!
  "Stops the default system.

  TODO: more doc"
  {:added      1.0
   :public-api true}
  []
  (core/set-system!
   (-> @core/system
       (core/stop-system)))
  (logger/info "System has been stopped successfully."))

(defn get-component
  "Finds and returns the component with the given `name`.

  TODO: more doc"
  {:added      1.0
   :public-api true}
  [name]
  (impl/get-component @core/system name))
