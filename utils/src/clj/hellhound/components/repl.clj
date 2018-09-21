(ns hellhound.components.repl
  (:require
   [nrepl.server :as nrepl-server]
   [hellhound.logger :as logger]
   [hellhound.component :as com]))

(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))


(defn start-fn
  [this context]
  (logger/info "Running nREPL on localhost:4000...")
  (assoc this
         :server
         (nrepl-server/start-server :port 4000 :handler (nrepl-handler))))

(defn stop-fn
  [this]
  (when-let [server (:server this)]
    (logger/info "Stopping nREPL...")
    (nrepl-server/stop-server server))
  (dissoc this :server))


(defn nrepl
  []
  (com/make-component ::nrepl start-fn stop-fn))
