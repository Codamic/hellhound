(ns hellhound.core
  "This namespace contains core functions which are required
  by the whole framework in order to operate."
  (:require [hellhound.config :as config]
            [clojure.java.io  :as io]))

;; Definitions ---------------------------------------------
(def environment-configuration (atom {}))

;; Functions -----------------------------------------------
;;;; Runtime Environment helpers ---------------------------
(defn env
  "Return the current runtime profile. Possible values
  `development`, `test`, `production` or any custom
  envrionment type which provided by `HH_ENV` environment
  variable. the default value is `:development`."
  []
  (keyword (or (System/getenv "HH_ENV") "development")))

(defn development?
  "Returns `true` if current runtime profile is `:development`."
  []
  (= :development (env)))

(defn test?
  "Returns `true` if current runtime profile is `:test`."
  []
  (= :test (env)))

(defn production?
  "Returns `true` if current runtime profile is `:production`."
  []
  (= :production (env)))


;;;; Runtime Environment Configuration Loaders -------------
(defn- config-file
  []
  (format "environments/%s.edn" (name (env))))

(defn load-runtime-configuration
  "Read and parse the configuration file related to the current runtime
  environment."
  []
  (let [config-data (config/read-config (config-file))]
    (reset! environment-configuration config-data)
    config-data))

(defn application-config
  "Return the current runtime environment configuration."
  []
  (let [config-data @environment-configuration]
    (if (empty? config-data)
      (load-runtime-configuration)
      config-data)))

(defn get-config
  "Fetch the given key (or nested keys) from the environment config of
  the project other. Returns the default value from hellhound.config"
  [& keys]
  (or (get-in (application-config) keys)
      (get-in config/default-config keys)))
