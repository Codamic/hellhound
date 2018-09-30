(ns hellhound.components.file-watcher
  (:require
   [hawk.core           :as hawk]
   [hellhound.streams   :refer [>>]]
   [hellhound.component :as com]))

(defn- handle-change
  [output-stream]
  (fn [hawk-ctx event]
    (println "event")
    (>> output-stream {:ctx hawk-ctx :event event})
    hawk-ctx))


(defn start-fn
  [config]
  (fn [this context]
    (println "start=fn")
    (assoc this
           :watcher
           (hawk/watch! [{:paths ["src/clj"] ;;(merge ["src/clj"] (or (:paths config) []))
                          :handler (handle-change (com/output this))}]))))


(defn stop-fn
  [this]
  (when-let [watcher (:watcher this)]
    (hawk/stop! watcher))
  (dissoc this :watcher))


(defn watcher-factory
  [config]
  (com/make-component ::watcher (start-fn config) stop-fn))


(defn change-handler
  [f]
  (fn [ctx event]
    (f event)
    ctx))

(defn start-watch
  [config f]
  (hawk/watch! [{:paths ["src/clj"] ;;(merge ["src/clj"] (or (:paths config) []))
                 :handler (change-handler f)}]))

(defn stop-watch
  [server]
  (hawk/stop! server))
