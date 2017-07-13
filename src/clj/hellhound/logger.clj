(ns hellhound.logger
  (:require [taoensso.timbre :as timbre]))


;; (defmacro info
;;   [& rest]
;;   `(timbre/info ~@rest))
(def ^{:macro true} info timbre/info)


(defn init!
  []
  (timbre/set-config!
   {:level :trace
    :enabled? true
    ;;:output-fn (fn [data] (println data) (str data))
    :appenders
    {:debug-appender {:enabled? true :min-level :trace :output-fn  :inherit
                      :fn (fn [data]
                            (let [{:keys [output_]} data
                                  formatted-output-str (force output_)]
                              (println formatted-output-str)))}}}))
