(ns hellhound.components.defaults
  "This namespace contains the default values for different parts of the
  system, for example default value for pedestal service."
  (:require
   [io.pedestal.http   :as http]
   [clojure.spec.alpha :as spec]
   [hellhound.core     :as hellhound]))

;; Vars ------------------------------------------
;; Default structure for a system map
(def system-structure
  {:components {}})

(defn services
  "Default configuration of services to be used inside the
  system map."
  []
  {
   ;; You can bring your own non-default interceptors. Make
   ;; sure you include routing and set it up right for
   ;; dev-mode. If you do, many other keys for configuring
   ;; default interceptors will be ignored.
   ;; ::http/interceptors []

   ;;::http/routes routes

   ;; In order to server secure content we need this change
   ;; TODO: In Pedestal 5.3.0 this configuration would be default
   ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}

   ;; Uncomment next line to enable CORS support, add
   ;; string(s) specifying scheme, host and port for
   ;; allowed source(s):
   ;;
   ;; "http://localhost:8080"
   ;;
   ;;::http/allowed-origins ["scheme://host:port"]

   ;; Root for resource interceptor that is available by default.
   ::http/resource-path "public"

   ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
   ::http/type :immutant


   ;; ;; Options to pass to the container (Immutant)
   ;; ::http/container-options

   ;;   {
   ;;    ;; Undertow tuning options (defaults depend on available resources)
   ;;    :io-threads
   ;;    :worker-threads
   ;;    :buffer-size
   ;;    :buffers-per-region
   ;;    :direct-buffers?

   ;;    ;; SSL-related options
   ;;    :ssl-port
   ;;    :ssl-context
   ;;    :key-managers
   ;;    :trust-managers
   ;;    :keystore (either file path or KeyStore)
   ;;    :key-password
   ;;    :truststore (either file path or KeyStore)
   ;;    :trust-password
   ;;    :client-auth (either :want or :need)}

   ::http/host (hellhound/get-config :http-host)
   ::http/port (hellhound/get-config :http-port)})
