(ns hellhound.config
  "This namespace contains core functions which are required
  by the whole framework in order to operate. Functions which
  provide easy way to read application configurations based
  on current environment. Default environment is `development`.
  In order to change the environment you need to set the `HH_ENV`
  environment variable to new value like `production`."
  (:require
   [hellhound.config.parser  :as parser]
   [hellhound.env            :as env]
   [hellhound.system.store   :as store]
   [hellhound.system.protocols :as impl]
   [hellhound.config.helpers :as helpers])


  #?(:cljs
     (:import [goog.string.format])))


;; Definitions ---------------------------------------------
(def environment-configuration (atom {}))

;;;; Runtime Environment Configuration Loaders -------------
(defn- config-file
  []
  (format "environments/%s.edn" (name (env/env))))

(defn load-runtime-configuration
  "Read and parse the configuration file related to the current runtime
  environment."
  []
  (let [config-data (parser/read-config (config-file))]
    (reset! environment-configuration config-data)
    config-data))

(defn application-config
  "Return the current runtime environment configuration."
  []
  (let [config-data @environment-configuration]
    (if (empty? config-data)
      (load-runtime-configuration)
      config-data)))


(defn get-config-from-system
  "Returns the value of the given `config-keys` by looking into the given
  `system-map`. If the value is `nil` it will return the possible
  default value from `hellhound.config`.
  "
  [system-map & config-keys]
  (let [config-value (impl/get-config system-map config-keys :not-found)]
    (if (= :not-found config-value)
      (helpers/default-value-for config-keys)
      config-value)))


(defn get-config
  "Fetch the given key (or nested keys) from the default system.
   If there is not value for the given `config-keys` in the system
   then it will return the default value from `hellhound.config`.

  Examples:

  ```clojure
  ;; Returns the value of `:http` from the config file
  (get-config :http)

  ;; Returns the value of `:host` key inside `:http` map.
  (get-config :http :host)
  ```
  "
  [& config-keys]
  (apply get-config-from-system (store/get-system) config-keys))
