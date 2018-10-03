(ns hellhound.core
  "This namespace contains several shortcut functions for other
  functions in different namespaces. In order to get more information
  checkout the documentation of each function in the original ns."
  (:require
   [hellhound.config :as config]
   [hellhound.env    :as env]))

;; ENV -----------------------------------------------------
(def ^{:original-ns :hellhound.env} env env/env)
(def ^{:original-ns :hellhound.env} development? env/development?)
(def ^{:original-ns :hellhound.env} test? env/test?)
(def ^{:original-ns :hellhound.env} production? env/production?)
