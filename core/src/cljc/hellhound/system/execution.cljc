(ns hellhound.system.execution
  (:require
   [hellhound.system.protocols :as impl])
  (:import
   [java.util.concurrent
    Executors
    TimeUnit]))


(def SINGLE_THREAD :single-thread)
(def MULTI_THREAD :multi-thread)

(defn execution-pool-size
  [system]
  ;; TODO: Try to guess number of processors and return a
  ;; pool size based on that or 8
  8)

(defn schedule-pool-size
  ([] 8)
  ([system]
   ;; TODO: Find the optimal number for the schedule size
   8))

(defn create-execution-pool
  ([]
   ;; TODO: Fix this function and pass system to underling fns
   (create-execution-pool (execution-pool-size {})))
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
  (= SINGLE_THREAD (:mode (execution-map system))))

(defn multi-threaded?
  [system]
  (= MULTI_THREAD (:mode (execution-map system))))

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
  [system delay f]
  (.schedule (impl/schedule-pool system) f delay TimeUnit/MILLISECONDS))

(defn schedule-fixrate-interval-with-system
  "Executes the given `f` periodically with the given `delay` regardless
  of the termination of the tasks and.

  It schedules the jobs on the schedule threadpool of the given `system`."
  [system delay f]
  (.scheduleAtFixedRate (impl/schedule-pool system)
                        f
                        0
                        delay
                        TimeUnit/MILLISECONDS))

(defn schedule-interval-with-system
  "Executes the given `f` periodically with the given `delay` between
  the termination of one execution and the commencement of the next.

  It schedules the jobs on the schedule threadpool of the given `system`."
  [system delay f]
  (.scheduleWithFixedDelay (impl/schedule-pool system)
                           f
                           0
                           delay
                           TimeUnit/MILLISECONDS))
