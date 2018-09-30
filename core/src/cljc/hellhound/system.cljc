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
  `system` map."
  {:added      1.0
   :public-api true}
  [system-map]
  (logger/init! (config/get-config-from-system system-map :logger))
  (store/set-system! system-map))

(defn system
  "Returns the processed system."
  {:added      1.0
   :public-api true}
  []
  (store/get-system))


(defn start
  [system-map]
  ;;(config/load-runtime-configuration)
  (let [new-system
        (-> system-map
            (core/init-system)
            (core/start-system)
            (workflow/setup))]
    (logger/info "System has been started successfully.")
    (println "returning the new system")
    new-system))

(defn start!
  []
  (alter-var-root #'hellhound.system.store/store
                  #(start %)))

;; (defn start!
;;   "Starts the default system by calling start on all the components.

;;   TODO: more doc"
;;   {:added      1.0
;;    :public-api true}
;;   []
;;   ;; Read the configuration for the current runtime environment which
;;   ;; specified by `HH_ENV` environment. Default env is `:development`
;;   (config/load-runtime-configuration)
;;   (store/set-system!
;;    (-> @store/system
;;        (core/init-system)
;;        (core/start-system)
;;        (workflow/setup)
;;        (core/shutdown-hook)))

;;   (logger/info "System has been started successfully."))


(defn stop
  [system-map]
  (when system-map
    (let [new-system
          (-> system-map
              (workflow/teardown)
              (core/stop-system))]
      (logger/info "System has been stopped successfully.")
      new-system)))

(defn stop!
  []
  (alter-var-root #'hellhound.system.store/store
                  #(stop %)))


;; (defn stop!
;;   "Stops the default system.

;;   TODO: more doc"
;;   {:added      1.0
;;    :public-api true}
;;   []
;;   (store/set-system!
;;    (-> @store/system
;;        (workflow/teardown)
;;        (core/stop-system)))
;;   (logger/info "System has been stopped successfully."))


(defn restart!
  []
  (store/set-system!
    (-> @store/system
        (core/restart-system))))


(defn get-component
  "Finds and returns the component with the given `name`.

  TODO: more doc"
  {:added      1.0
   :public-api true}
  [component-name]
  (impl/get-component @store/system component-name))
