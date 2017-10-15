(ns hellhound.system.workflow
  "System's workflow a vector describing the dataflow of the system.
  Components have an input and an output stream. Each stream is a
  `manifold.stream`. HellHound connects io of each component to another
  component based on the desciption given by the `:workflow` of the
  system.

  System's workflow is a vector of vectors. Each vector contains two
  mandatory element which are:
    * The name of the output component
    * The name of the input component
  and an optional predicate function. This function connects the
  output stream of output component to input stream of input component,
  and in case of existance of a predicate function, it only sends those
  messages which pass the predicate.

  Predicate function should be a pure function obviousely."
  (:require [manifold.stream        :as stream]
            [manifold.deferred      :as d]
            [hellhound.logger       :as log]
            [hellhound.system.utils :as utils]
            [hellhound.component    :as hcomp])

  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn- parse-triple
  "Returns a vector of workflow by fetch the corresponding manifold stream
  and the given predicate function.

  For example if the the given workflow vector be like:

  [:input/component :output/component]

  it's goint to return a vector with the same order but containing the
  output stream of `:input/component` and input stream of `:output/compnoent`.
  If the workflow vector contained any predicate function this the return value
  of this function will contain the predicate too."
  ([components sink-name source-name]
   (let [sink   (hcomp/output (get components sink-name))
         source (hcomp/input (get components source-name))]
     [sink source]))

  ([components sink-name pred source-name]
   (let [[sink source] (parse-triple components sink-name source-name)]
     [sink pred source])))

(defn- invalid-workflow
  [component]
  (throw (Exception. (format "Invalid compponent '%s' in workflow."
                              (hcomp/get-name component)))))
(defn message-router
  "Applies the given `pred` function on incoming `msg` and sends it to
  downstream if the function returned true."
  [source pred msg]
  (if (pred msg)
    (stream/put! source msg)
    ;; In order to drop the msg we need to return a deferred which resolves
    ;; to true.
    (let [fake-result (d/deferred)]
      (d/success! fake-result true)
      fake-result)))

(defn ^PersistentVector get-workflow
  "Returns the workflow vectors of the given `system`."
  [^IPersistentMap system]
  (:workflow system))

(defn connect
  "Connects the given `sink` to the given `source`."
  ([sink source]
   (stream/connect sink source))

  ([sink pred source]
   (stream/connect-via sink
                       #(message-router source pred %)
                       source)))

(defn wire-io!
  "Walks through the workflow vectors and wire up the system workflow
  based on desciption given by each vector.

  System's workflow is a vector of vectors. Each vector contains two
  mandatory element which are:
    * The name of the output component
    * The name of the input component
  and an optional predicate function. This function connects the
  output stream of output component to input stream of input component,
  and in case of existance of a predicate function, it only sends those
  messages which pass the predicate."
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
