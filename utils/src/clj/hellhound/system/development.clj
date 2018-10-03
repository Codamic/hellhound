;; (ns hellhound.system.development
;;   (:require
;;    [hellhound.components.file-watcher :as watcher]
;;    [hellhound.components.pretty-printer :as printer]
;;    [hellhound.components.namespace :as nspace]
;;    [hellhound.config :as config]
;;    [hellhound.system.core :as syscore]
;;    [hellhound.system :as sys]))


;; (defn system-watcher-factory
;;   [original-system]
;;   (let [watcher-config (config/get-config-from-system original-system
;;                                                       [:watcher]
;;                                                       {})
;;         ns-loader-config (config/get-config-from-system original-system
;;                                                         [:ns-loader]
;;                                                         {})]

;;     {:components [(watcher/watcher-factory watcher-config)
;;                   (nspace/loader-factory original-system ns-loader-config)]

;;      :workflow [[::watcher/watcher
;;                  ::nspace/loader]]

;;      :execution {:mode :multi-thread}
;;      :logger {:level :trace}}))

;; (defn start!
;;   [system-var]
;;   (println "starting ...............................")
;;   (watcher/start-watch system-var
;;                        (nspace/refresh system-var {}))
;;   (print "STARTED"))


;; (defn stop!
;;   [server]
;;   (watcher/stop-watch server))


(ns hellhound.system.development
  (:require
   [hellhound.system :as sys]
   [clojure.tools.namespace.repl :as repl]))


(defn load-and-set-system!
  [system-path]
  (assert (symbol? system-path) "'system-path' value must be a symbol")
  (assert (namespace system-path)
          "'system-path' value must be a namespace-qualified symbol")
  (let [system-fn (ns-resolve *ns* system-path)]
    (hellhound.system.store/set-system! system-fn)))

(defmacro setup-development
  [nsname system-var]
  `(do
     (defn ~(symbol "start")
       []
       (hellhound.system.development/load-and-set-system! ~system-var)
       (hellhound.system/start1))

     (defn ~(symbol "stop")
       []
       (hellhound.system/stop1))

     (defn ~(symbol "restart")
       []
       (hellhound.system/stop1)
       (clojure.tools.namespace.repl/refresh :after  '~(symbol (str nsname "/start"))))))
