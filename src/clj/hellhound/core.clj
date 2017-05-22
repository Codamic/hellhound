(ns hellhound.core
  "This namespace contains core functions which are required
  by the whole framework in order to operate.")

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
