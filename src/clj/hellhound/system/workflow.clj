(ns hellhound.system.workflow
  "TODO"
  (:require [clojure.pprint         :as pp]
            [manifold.stream        :as stream]
            [hellhound.logger       :as log]
            [hellhound.system.utils :as utils]
            [hellhound.component    :as hcomp])

  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn ^PersistentVector get-workflow
  [^IPersistentMap system]
  (vec (:components-workflow system)))

(defn invalid-workflow
  [component]
  (throw (Exception. (format "Invalid compponent '%s' in workflow."
                              (hcomp/get-name component)))))

(defn connect
  [sink source]
  (let [output (hcomp/output sink)
        input  (hcomp/input  source)]
    (log/debug
     (format "Connecting output of '%s' to input of '%s'..."
             (hcomp/get-name sink)
             (hcomp/get-name source)))
    (stream/connect sink source)))

(defn wire-io!
  ([^IPersistentMap components ^IPersistentMap workflow]
   (wire-io! components (rest workflow) (first workflow)))

  ([^IPersistentMap components ^IPersistentMap workflow component-pair]
   (when component-pair
     (let [sink    (get components (first component-pair))
           sources (map #(get components %) (second component-pair))]

       (when (nil? sink) (invalid-workflow (first component-pair)))

       (doseq [source sources]
         (when (nil? source) (invalid-workflow source))
         (connect sink source))

       (recur components (rest workflow) (first workflow))))))

(defn ^IPersistentMap setup
  "Sets up the workflow of the system by wiring the io of each component
  in the order provided by the user in `:workflow` key."
  [^IPersistentMap system]
  (log/debug "Setting up workflow...")
  (wire-io! (utils/get-components system)
            (get-workflow system))
  (log/info "Workflow setup done."))

(defn ^PersistentVector walk-graph
  [^IPersistentMap parsed-map ^PersistentVector pair]
  (assoc parsed-map
         (first pair)
         (conj (get parsed-map (first pair)) (second pair))))

(defn ^IPersistentMap workflow-map
  "Parse the workflow graph and returns a hashmap which keys
  are the components name and the values are inputs for that component."
  [^IPersistentMap system-map]
  (reduce walk-graph {} (:workflow system-map)))
