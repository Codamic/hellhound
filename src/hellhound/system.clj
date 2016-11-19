(ns hellhound.system
  "A thin wrapper layer on top of `danielsz/system`."
  (:require [system.repl :refer [system]]))

(defn get-system
  "Return the `system.repl.system`. This is a simple
  wrapper on top of `system.repl` in order to centeralize
  everything in one place."
  []
  system)
