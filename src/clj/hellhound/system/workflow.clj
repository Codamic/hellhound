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

(defn input-of
  [components component-name]
  (let [component (get components component-name)]
    (if (not component)
      (throw (Exception. (str "Invalid compponent '" component-name "' in workflow.")))
      (hcomp/output component))))

(defn wire-io!
  ([^IPersistentMap components ^IPersistentMap workflow]
   (wire-io! components (rest workflow) (first workflow)))

  ([^IPersistentMap components ^IPersistentMap workflow component-pair]
   (when component-pair
     (let [component-name (first component-pair)
           targets        (second component-pair)
           component      (get components component-name)
           output         (hcomp/output component)
           inputs         (map #(input-of components %) targets)]

       (map (fn [input]
              (log/info "Wiring output of '" component-name "'...")
              (stream/connect output input)) inputs)
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
