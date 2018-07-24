(ns hellhound.env
  "This namespace contains several helper functions related to runtime
  environment mode. For example `development?` function returns true
  if we are running on development mode.

  **HellHound** uses `HH_ENV` envrionment variable for runtime environment.")


(defn- get-env
  [env-key]
  #?(:clj (System/getenv env-key)
     :cljs (aget js/process.env env-key)))


;;;; Runtime Environment helpers ---------------------------
(defn env
  "Return the current runtime profile. Possible values
  `development`, `test`, `production` or any custom
  envrionment type which provided by `HH_ENV` environment
  variable. the default value is `:development`."
  []
  (keyword (or (get-env "HH_ENV") "development")))

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
