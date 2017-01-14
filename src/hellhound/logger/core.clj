(ns hellhound.logger.core
  (:require [potemkin :refer [import-vars]]
            [taoensso.timbre :as t]))

(defmacro warn [& args] `(t/warn ~@args))
(defmacro info [& args] `(t/info ~@args))
(defmacro error [& args] `(t/error ~@args))
(defmacro log [& args] `(t/log ~@args))
(defmacro fatal [& args] `(t/fatal ~@args))
(defmacro debug [& args] `(t/debug ~@args))
(defmacro trace [& args] `(t/trace ~@args))


;; (import-vars [taoensso.timbre
;;               log  trace  debug  info  warn  error  fatal  report
;;               logf tracef debugf infof warnf errorf fatalf reportf spy])
