(ns hellhound.system
  "A thin wrapper layer on top of `danielsz/system`."
  (:require [system.repl :as sys]))

(def system sys/system)
