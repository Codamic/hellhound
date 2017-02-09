(ns hellhound.middlewares.logger
  "Log requests and responses for better development."
  (:require [colorize.core         :as color]
            [hellhound.logger.core :as logger]))

;; TODO: We need to refactore this function to include the parameterize
;;       keys and values
(defn wrap-logger
  "Log the requests and responses."
  [handler]
  (fn [request]
    (let [start (System/currentTimeMillis)]
      (logger/info (format "%s %s %s, %s %s"
                           (color/bold (color/white "[REQUEST]"))
                           (color/blue "Started,")
                           (color/bold (color/yellow (clojure.string/upper-case (:request-method request))))
                           (color/green "PATH:")
                           (:uri request)))

      (let [response (handler request)
            status   (:status response)
            finish   (System/currentTimeMillis)
            total    (- finish start)]

        (logger/info (format "%s %s %s %s %s %s"
                             (color/bold (color/white "[Response]"))
                             (color/cyan "Finished,")
                             (color/magenta "TIME:")
                             (str  total "ms")
                             (color/white "Status:")
                             (cond
                               (and (< status 300) (> status 199)) (color/green status)
                               (and (< status 600) (> status 399)) (color/red   status)
                               :else (color/yellow status))))
        response))))
