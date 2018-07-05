(ns hellhound.system.execution
  (:require
   [hellhound.system.protocols :as impl])
  (:import
   [java.util.concurrent Executors]))


(def SINGLE_THREAD :single-thread)
(def MULTI_THREAD :multi-thread)

(def execution-pool-size
  [system]
  ;; TODO: Try to guess number of processors and return a
  ;; pool size based on that or 8
  8)

(def schedule-pool-size
  [system]
  ;; TODO: Find the optimal number for the schedule size
  8)

(defn create-execution-pool
  ([]
   (create-execution-pool (execution-pool-size)))
  ([size]
   (Executors/newFixedThreadPool size)))

(defn create-wait-pool
  []
  (Executors/newCachedThreadPool))

(defn create-schedule-pool
  ([]
   (create-schedule-pool (schedule-pool-size)))
  ([size]
   (Executors/newScheduledThreadPool size)))


(defn default-execution-pool
  [system]
  ;; Take the pool configuration of the system into account for
  ;; calculating the size of the pool.
  (delay (create-execution-pool)))

(defn default-wait-pool
  [system]
  ;; Take the pool configuration of the system into account for
  ;; calculating the size of the pool.
  (delay (create-wait-pool)))

(defn default-schedule-pool
  [system]
  ;; Take the pool configuration of the system into account for
  ;; calculating the size of the pool.
  (delay (create-schedule-pool)))

(defn execution-pool
  [system]
  (impl/execution-pool system))

(defn wait-pool
  [system]
  (impl/wait-pool system))

(defn schedule-pool
  [system]
  (impl/schedule-pool system))

(defn execution-map
  [system]
  (or (:execution system) {}))

(defn single-threaded?
  [system]
  (= SINGLE_THREAD (:mode (execution-map))))

(defn multi-threaded?
  [system]
  (= MULTI_THREAD (:mode (execution-map))))

(defn execute-in
  [executor f]
  (.execute executor f))

(defn execute-with-system
  [system f]
  (execute-in (impl/execution-pool system) f))

(defn execute-io-with-system
  [system f]
  (execute-in (impl/wait-pool system) f))

(defn schedule-with-system
  [system f details]
  (execute-in (impl/schedule-pool system) f))

(comment
  (let [threadpool (fn [size]
                     (Executors/newFixedThreadPool size))

        a (system {:components []
                   :workflow []
                   :execution {:mode :multi-thread
                               :execution-pool (threadpool 3)
                               :wait-pool}})]))
