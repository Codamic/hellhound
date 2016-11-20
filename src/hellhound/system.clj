(ns hellhound.system
  "A thin wrapper layer on top of `danielsz/system`."
  (:require [system.repl :refer [system]]
            [com.stuartsierra.component :refer [system-map]]))

(defn get-system
  "Return the `system.repl.system`. This is a simple
  wrapper on top of `system.repl` in order to centeralize
  everything in one place."
  []
  system)


(defmacro defsystem
  [name & body]
  `(defn ~name [] (-> (system-map) ~@body)))
