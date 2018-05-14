(ns hellhound.env)

(defn- get-env
  [key]
  #?(:clj (System/getenv key)
     :cljs (aget js/process.env key)))

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
