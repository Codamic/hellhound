(ns hellhound.components.file-watcher
  (:require
   [hawk.core           :as hawk]
   [hellhound.streams   :refer [>>]]
   [hellhound.component :as com]))

(defn- handle-change
  [output-stream]
  (fn [hawk-ctx event]
    (>> output-stream {:ctx hawk-ctx :event event})
    hawk-ctx))

(defn start-fn
  [this context]
  (assoc this
         :watcher
         (hawk/watch! [{:paths ["src/clj"]
                        :handler (handle-change (com/output this))}])))

(defn stop-fn
  [this]
  (when-let [watcher (:watcher this)]
    (hawk/stop! watcher))
  (dissoc this :watcher))

(def watcher (com/make-component ::watcher start-fn stop-fn))
