(ns hellhound.logger.formatters
  "Different types of log formatter functions to be used with
  timbre for different environments."
  {:author "Sameer Rahmani (@lxsameer)"}
  (:require [taoensso.timbre :as timbre]))

(def LEVELS {:trace (timbre/color-str :cyan   "TRACE")
             :debug (timbre/color-str :blue   "DEBUG")
             :info  (timbre/color-str :green  "INFO")
             :warn  (timbre/color-str :yellow "WARN")
             :error (timbre/color-str :red    "ERROR")
             :fatal (timbre/color-str :purple "FATAL")})

(defn ^String process-level
  [^clojure.lang.Keyword level-keyword]
  (level-keyword LEVELS))


(defn ^String default-dev-formatter
  [^clojure.lang.PersistentArrayMap data]
  (let [time   @(:timestamp_ data)
        line   (:?line data)
        level  (process-level (:level data))
        ns-str (:?ns-str data)
        msg    @(:msg_ data)]
    (format "[%s] [%s] <%s:%s> - %s" time level ns-str line msg)))
