(ns hellhound.async.dispatch
  (:require [clojure.core.async.impl.protocols :as impl]
            [clojure.core.async.impl.exec.threadpool :as tp]))

(set! *warn-on-reflection* true)

(defonce executor (delay (tp/thread-pool-executor)))

(defn run
  "Runs Runnable r in a thread of the given thread pool."
  ([^Runnable r]
   (run r executor))

  ([^Runnable r executor_]
   (impl/exec @executor_ r)))
