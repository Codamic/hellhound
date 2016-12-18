(ns hellhound.middlewares.logger
  (:require [colorize.core         :as color]
            [hellhound.logger.core :as logger]))

(defn wrap-logger [handler]
  (fn [request]
    (logger/info (format "[REQUEST] %s %s" (color/green "PATH: ") (:uri request)))
    (handler request)))
