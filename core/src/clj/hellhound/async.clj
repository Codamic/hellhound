(ns hellhound.async
  (:require
   [clojure.core.async.impl.ioc-macros :as ioc]
   ;; TODO Replace tp with our own implementation.
   [clojure.core.async.impl.exec.threadpool :as tp]
   ;; TODO: Remove the async requirement here
   [clojure.core.async :refer [chan <!]]
   [hellhound.async.dispatch :as dispatch]))

(def executor1 (delay (tp/thread-pool-executor)))

(defmacro go-block
  "Asynchronously executes the given `body` in a thread pool dedicated
  to blocking operations.

  I will return immediately to the calling thread. Additionally, any visible
  calls to <!, >! and alt!/alts! channel operations within the body will block
  (if necessary) by 'parking' the calling thread rather than tying up an OS
  thread (or the only JS thread when in ClojureScript). Upon completion of the
  operation, the body will be resumed.

  Returns a channel which will receive the result of the body when
  completed."
  [& body]
  (let [crossing-env (zipmap (keys &env) (repeatedly gensym))]
    (println "xxxxxxxxxxxxxxxxxxx")
    (clojure.pprint/pprint crossing-env)
    `(let [c# (chan 1)
           captured-bindings# (clojure.lang.Var/getThreadBindingFrame)]

       (println "yyyyyyyyyyy")
       (dispatch/run
         (^:once fn* []
          (let [~@(mapcat (fn [[l sym]] [sym `(^:once fn* [] ~(vary-meta l dissoc :tag))]) crossing-env)
                f# ~(ioc/state-machine `(do ~@body) 1 [crossing-env &env] ioc/async-custom-terminators)
                state# (-> (f#)
                           (ioc/aset-all! ioc/USER-START-IDX c#
                                          ioc/BINDINGS-IDX captured-bindings#))]
            (ioc/run-state-machine-wrapped state#)))
         executor1)
       c#)))



(let [b (chan 1)
      a (go-block (println "----------------") (<! b))])
