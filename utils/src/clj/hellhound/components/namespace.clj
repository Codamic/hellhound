(ns hellhound.components.namespace
  (:require
   [clojure.tools.namespace.repl :as repl]
   [hellhound.component :refer [deftransform]]))

(defn reload-ns
  []
  (binding [*ns* *ns*]
    (repl/refresh)))

;; TODO: Refactor this function
(defn should-reload?
  [kind file]
  (and (or (= kind :modify)
           (= kind :create))
       (and file
            (not (re-matches #"\..*" (.getName file))))))

(deftransform loader
  [component value]
  (let [{:keys [kind file]} (:event value)]
    (cond
      (should-reload? kind file) (reload-ns)
      ;; TODO: Replace the not reloading with a log entry.
      :else (println "NOt reloading"))))
