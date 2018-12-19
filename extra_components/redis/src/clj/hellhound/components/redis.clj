(ns hellhound.components.redis
  (:require
   [taoensso.carmine :as car]
   [hellhound.streams :as streams]
   [hellhound.component :as com]))


(defn start
  [component context]
  (let [config (or (:config context) {})]
    (assoc component
           :connection-details config)))

(defn stop!
  [component]
  (dissoc component :connection-details))

(defn execute-redis-command
  [component v]
  v)


(defn transformer
  [component]
  (streams/consume
   (fn [v]
     (let [processed-v (execute-redis-command component v)]
       (when processed-v
         (streams/>> (output component) processed-v))))
   (input component)))

(def client
  {::com/name ::client
   ::com/start-fn start
   ::com/stop-fn  stop
   ::com/fn transformer
   ::com/doc "something"
   ::com/input-spec ::input-spec
   ::com/output-spec ::output-spec})
