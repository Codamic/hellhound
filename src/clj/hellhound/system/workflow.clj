(ns hellhound.system.workflow
  "TODO"
  (:require [manifold.stream        :as stream]
            [manifold.deferred      :as d]
            [hellhound.logger       :as log]
            [hellhound.system.utils :as utils]
            [hellhound.component    :as hcomp])

  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn- parse-triple
  ([components sink-name source-name]
   (let [sink   (hcomp/output (get components sink-name))
         source (hcomp/input (get components source-name))]
     [sink source]))

  ([components sink-name pred source-name]
   (let [[sink source] (parse-triple components sink-name source-name)]
     [sink pred source])))

(defn message-router
  [source pred msg]
  (if (pred msg)
    (stream/put! source msg)
    (let [fake-result (d/deferred)]
      (d/success! fake-result true)
      fake-result)))

(defn ^PersistentVector get-workflow
  [^IPersistentMap system]
  (:workflow system))

(defn invalid-workflow
  [component]
  (throw (Exception. (format "Invalid compponent '%s' in workflow."
                              (hcomp/get-name component)))))

(defn connect
  ([sink source]
   (stream/connect sink source))

  ([sink pred source]
   (stream/connect-via sink
                       #(message-router source pred %)
                       source)))

(defn wire-io!
  ([^IPersistentMap components ^IPersistentMap workflow]
   (wire-io! components (rest workflow) (first workflow)))

  ([^IPersistentMap components ^IPersistentMap workflow workflow-triple]
   (when workflow-triple
     (let [component-tuple (apply parse-triple components workflow-triple)]
       (apply connect component-tuple))
     (recur components (rest workflow) (first workflow)))))

(defn ^IPersistentMap setup
  "Sets up the workflow of the system by wiring the io of each component
  in the order provided by the user in `:workflow` key."
  [^IPersistentMap system]
  (log/debug "Setting up workflow...")
  (wire-io! (utils/get-components system)
            (get-workflow system))
  (log/info "Workflow setup done."))
