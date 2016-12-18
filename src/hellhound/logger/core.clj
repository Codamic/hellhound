(ns hellhound.logger.core
  (:require [potemkin :refer [import-vars]]))



(import-vars [taoensso.timbre
              log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf spy])
