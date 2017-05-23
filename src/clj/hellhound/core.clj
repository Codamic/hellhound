(ns hellhound.core
  "This namespace contains core functions which are required
  by the whole framework in order to operate."
  (:require [hellhound.config :refer [read-config]]
            [clojure.java.io :as io]))

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
  (let [config (read-config (config-file))]
    (reset! environment-configuration config)
    config))

(defn application-config
  "Return the current runtime environment configuration."
  []
  @environment-configuration)
