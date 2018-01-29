(ns hellhound.logger.formatters
  "Different types of log formatter functions to be used with
  timbre for different environments."
  {:author "Sameer Rahmani (@lxsameer)"}
  (:require [taoensso.timbre :as timbre]))

(defn red
  [msg]
  (timbre/color-str :red msg))

(defn yellow
  [msg]
  (timbre/color-str :yellow msg))

(defn green
  [msg]
  (timbre/color-str :green msg))

(defn cyan
  [msg]
  (timbre/color-str :cyan msg))

(defn blue
  [msg]
  (timbre/color-str :blue msg))

(defn purple
  [msg]
  (timbre/color-str :purple msg))


(def LEVELS {:trace (cyan   "TRACE")
             :debug (blue   "DEBUG")
             :info  (green  "INFO")
             :warn  (yellow "WARN")
             :error (red    "ERROR")
             :fatal (purple "FATAL")})

(defn ^String process-level
  [^clojure.lang.Keyword level-keyword]
  (level-keyword LEVELS))

(defn ^String format-msg
  [data]
  (let [time   @(:timestamp_ data)
        line   (:?line data)
        level  (process-level (:level data))
        ns-str (:?ns-str data)
        msg    @(:msg_ data)]
    (format "[%s] [%s] <%s:%s> - %s" time level ns-str line msg)))


(defn format-single-trace
  [config [ns-str action file-name line]]
  (let [highlight (filter #(re-matches (re-pattern %) (str ns-str))
                           (:important-namespaces config))]


    (format "%s @ %s:%s -> %s"
            (if (empty? highlight) ns-str (red ns-str))
            (cyan file-name)
            (yellow line)
            action)))

(defn join-stracktrace
  [config trace]
  (clojure.string/join "\n" (map #(format-single-trace config %) trace)))

(defn ^String format-error
  [config data err]
  (let [msg-format (format-msg data)
        via        (first (:via err))
        err-type   (:type via)
        err-msg    (:message via)
        err-at     (:at via)
        trace      (:trace err)]

    (str msg-format
         "\n\n"
         (format "%s: %s @ %s\n" (red err-type) err-msg (yellow err-at))
         (red "\n---- TRACE ----\n")
         (join-stracktrace config trace)
         (red "\n---------------\n"))))

(defn ^String default-dev-formatter
  [^clojure.lang.PersistentArrayMap config]
  (fn [^clojure.lang.PersistentArrayMap data]
    (let [err    (:?err data)]
      (if err
        (format-error config data (Throwable->map err))
        (format-msg data)))))
