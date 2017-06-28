(ns hellhound.core
  (:require [hellhound.config :as config]
            [hellhound.env    :as env]))

;; Shortcut functions --------------------------------------
(def get-config config/get-config)
(def env env/env)
