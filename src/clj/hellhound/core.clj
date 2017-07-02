(ns hellhound.core
  (:require [hellhound.config :as config]
            [hellhound.env    :as env]))

;; Shortcut functions --------------------------------------
(def get-config config/get-config)

;; ENV
(def env env/env)
(def development? env/development?)
(def test? env/test?)
(def production? env/production?)
