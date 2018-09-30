(ns hellhound.components.namespace
  (:require
   [clojure.tools.namespace.repl :as repl]
   [hellhound.system :as sys]
   [hellhound.logger :as logger]
   [hellhound.component :as com]))


(defn reload-ns
  [config]
  (binding [*ns* *ns*]
    (repl/refresh)))


;; TODO: Refactor this function
(defn should-reload?
  [kind file]
  (and (or (= kind :modify)
           (= kind :create))
       (and file
            (not (re-matches #".*\.#[^/]+(clj|cljc)$" (.getName file))))))


(defn refresh
  [system-var config]
  (fn [event]
    (println "refrefref")
    (let [{:keys [kind file]} event]
      (when (should-reload? kind file)
        (logger/info "Stoping system for reloading...")
        (sys/stop!)
        (reload-ns config)
        (logger/info "Reloaded, Restarting the system...")
        (clojure.pprint/pprint ((deref system-var)))
        (sys/set-system! ((deref system-var)))
        (sys/start!)))))
