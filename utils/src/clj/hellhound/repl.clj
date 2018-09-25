(ns hellhound.repl
  (:require
   [nrepl.server :as nrepl-server]
   [hellhound.logger :as logger]
   [hellhound.system :as sys]))

(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))


(defn start!
  []
  (logger/info "Running nREPL on localhost:4000...")
  (sys/set-system!
   (assoc (sys/system)
          ::server
          (nrepl-server/start-server :port 4000 :handler (nrepl-handler)))))


(defn stop!
  []
  (when-let [server (::server (sys/system))]
    (logger/info "Stopping nREPL...")
    (nrepl-server/stop-server server))
  (sys/set-system! (dissoc (sys/system) ::server)))
