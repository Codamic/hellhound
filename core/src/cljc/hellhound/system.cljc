(ns hellhound.system
  "Systems are the most important thing in the **HellHound** ecosystem.
  Systems define how your application should work."
  ^{:author "Sameer Rahmani (@lxsameer)"
    :added 1.0}
  (:require [hellhound.config              :as config]
            [hellhound.logger              :as logger]
            [hellhound.config.defaults     :as default]
            [hellhound.system.protocols    :as impl]
            [hellhound.system.core         :as core]
            [hellhound.system.store        :as store]
            [hellhound.system.workflow     :as workflow]))


(defn set-system!
  "Sets the default system of HellHound application to the given
  `system-map`."
  {:added      1.0
   :public-api true}
  [system-map]
  ;; TODO: We need to establish an official entry point for the system
  ;;       and move the logger initialization to there.
  (logger/init! (config/get-config-from-system system-map :logger))
  (store/set-system! system-map))


(defn system
  "Returns the processed system which is set as the application wide
  system."
  {:added      1.0
   :public-api true}
  []
  (store/get-system))


(defn start
  "Starts the given `system-map` by initalizing the system and call the
  `start-fn` of all the components in order and setting up the workflow
  by piping components IO together. It returns the started system map."
  [system-map]
  (let [new-system
        (-> system-map
            (core/init-system)
            (core/start-system)
            (workflow/setup))]
    (logger/info "System has been started successfully.")
    new-system))

(defn start!
  "Starts the default system of application which is stored in
  `hellhound.system.store/store` by passing its value to `start`
  function and change the root of the store to the started system.
  Basically replace the old system with the started system."
  []
  (alter-var-root #'hellhound.system.store/store
                  #(start %)))


(defn stop
  "Stops the given `system-map` by walking the dependency tree and call the
  `stop-fn` of the components in order and teardown the workflow. It returns
  the stopped system."
  [system-map]
  (when system-map
    (let [new-system
          (-> system-map
              (workflow/teardown)
              (core/stop-system))]
      (logger/info "System has been stopped successfully.")
      new-system)))

(defn stop!
  "Stops the default system of application which is stored in
  `hellhound.system.store/store` and sets the root of the store
  to the stopped system. Basically stops and replaces the default
  system."
  []
  (alter-var-root #'hellhound.system.store/store
                  #(stop %)))


(defn get-component
  "Finds and returns the component with the given `component-name`
  in the default system which is stored in `hellhound.system.store/store`."
  {:added      1.0
   :public-api true}
  [component-name]
  (impl/get-component store/store component-name))
