(ns hellhound.logger.formatters
  "Different types of log formatter functions to be used with
  timbre for different environments."
  {:author "Sameer Rahmani (@lxsameer)"}
  (:require [taoensso.timbre :as timbre]))

(defn- color
  [code]
  (format "\u001b[%sm"
          (case code
            :reset  "0"
            :bold   "1"
            :italic "3"
            :underline "4"
            :black  "30" :red   "31"
            :green  "32" :yellow "33" :blue  "34"
            :purple "35" :cyan   "36" :white "37"
            :else   "0")))

(defn color-msg
  [color-name & xs]
  (str (color color-name) (apply str xs) (color :reset)))

(defn color-msg-with-style
  [style color-name & xs]
  (apply color-msg color-name (color style) xs))

(defn red
  [msg]
  (color-msg :red msg))


(defn bold-red
  [msg]
  (color-msg-with-style :bold :red msg))


(defn yellow
  [msg]
  (color-msg :yellow msg))


(defn bold-yellow
  [msg]
  (color-msg-with-style :bold :yellow msg))


(defn green
  [msg]
  (color-msg :green msg))


(defn bold-green
  [msg]
  (color-msg-with-style :bold :green msg))


(defn cyan
  [msg]
  (color-msg :cyan msg))


(defn bold-cyan
  [msg]
  (color-msg-with-style :bold :cyan msg))


(defn blue
  [msg]
  (color-msg :blue msg))


(defn bold-blue
  [msg]
  (color-msg-with-style :bold :blue msg))


(defn purple
  [msg]
  (color-msg :purple msg))


(defn bold-purple
  [msg]
  (color-msg-with-style :bold :purple msg))


(defn white
  [msg]
  (color-msg :white msg))


(defn bold-white
  [msg]
  (color-msg-with-style :bold :white msg))



(def LEVELS {:trace (white       "TRACE")
             :debug (bold-blue   "DEBUG")
             :info  (bold-green  "INFO")
             :warn  (bold-yellow "WARN")
             :error (red         "ERROR")
             :fatal (bold-purple "FATAL")})

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
    (format "(%s) [%s] <%s:%s>: %s"
            time
            level
            (purple ns-str)
            (yellow line)
            msg)))


(defn format-single-trace
  [config [ns-str action file-name line]]
  (let [highlight (filter #(re-matches (re-pattern %) (str ns-str))
                          (:important-namespaces config))]

    (format "%s:%s -->  %s/%s"
            (if (empty? highlight)
              (purple file-name)
              (bold-red file-name))
            line
            (if (empty? highlight)
              (yellow ns-str)
              (bold-red ns-str))
            action)))


(defn join-stracktrace
  [config trace]
  (clojure.string/join "\n\t" (map #(format-single-trace config %) trace)))

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
         (red "\n TRACEBACK -------------------------\n")
         (format "\t%s" (join-stracktrace config trace))
         "\n\n"
         (format "In %s\n%s: %s" err-at (bold-red err-type) err-msg)
         (red "\n------------------------------------\n"))))

(defn ^String default-dev-formatter
  [^clojure.lang.PersistentArrayMap config]
  (fn [^clojure.lang.PersistentArrayMap data]
    (let [err    (:?err data)]
      (if err
        (format-error config data (Throwable->map err))
        (format-msg data)))))
