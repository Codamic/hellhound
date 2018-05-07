(ns hellhound.system.workflow
  "System's workflow a vector describing the dataflow of the system.
  Components have an input and an output stream. Each stream is a
  core.async channel. HellHound connects io of each component to another
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
  (:require [hellhound.system.impl.splitter :as spltr]
            [hellhound.system.protocols     :as impl]
            [hellhound.logger               :as log]
            [hellhound.system.utils         :as utils]
            [hellhound.component            :as hcomp])

  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn- invalid-workflow
  [component]
  (throw (ex-info (format "Invalid component '%s' in workflow."
                          (hcomp/get-name component))
                  {:cause component})))

(defn- invalid-component-name
  [cname]
  (throw (ex-info
          (format "Can't find component '%s' in the system." cname)
          {:cause cname})))

(defn- parse-triple
  "Returns a vector of workflow by fetch the corresponding manifold stream
  and the given predicate function.

  For example if the the given workflow vector be like:

  [:input/component :output/component]

  it's goint to return a vector with the same order but containing the
  output stream of `:input/component` and input stream of `:output/compnoent`.
  If the workflow vector contained any predicate function this the return value
  of this function will contain the predicate too."
  ([splitter components sink-name source-name]
   (let [
         sink-component   (get components sink-name)
         source-component (get components source-name)]

     (when (nil? sink-component) (invalid-component-name sink-name))
     (when (nil? sink-component) (invalid-component-name source-name))

     [(hcomp/output sink-component) (hcomp/input source-component)]))


  ([components sink-name pred source-name]
   (let [[sink source] (parse-triple components sink-name source-name)]
     [sink pred source])))

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
  ([splitter to]
   (impl/connect splitter to))

  ([splitter pred to]
   (impl/connec splitter to {:filter-fn pred}))

  ([splitter pred map-fn to
    (impl/connec splitter to {:filter-fn pred
                              :map-fn    map-fn})]))

(defn wire-io1!
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
     (let [component-vec (apply parse-triple components workflow-triple)]
       (apply connect component-vec))
     (recur components (rest workflow) (first workflow)))))


(defn parse
  "Returns a operations map based on the given arguments."
  ([from to]
   (parse from #(identity %) #(identity %) to))

  ([from pred to]
   (parse from pred #(identity %) to))

  ([from pred map-fn to]
   [from (spltr/make-ops-map pred map-fn) to]))

(defn make-splitter
  "Creates a splitter from the given `source-component` component."
  [components source-component]
  (spltr/make-splitter (hcomp/output source-component)))

(defn connect-workflow
  "Setup and connect the components through the splitters."
  [[splitters components] connection-vec]

  (let [[from ops-map to] (apply parse connection-vec)
        source-component  (get components from)
        dest-componen     (get components to)]

    ;; Validates the source and dest components
    (when (nil? source-component)
      (invalid-component-name from))

    (when (nil? dest-component)
      (invalid-component-name to))


    ;; Get or create a new splitter from the source
    ;; component
    (let [splitter (or (get splitters from)
                       (make-splitter source-component))])

    (impl/connect splitter
                  (hcomp/input dest-component)
                  ops-map)


    [(assoc splitters from splitter)
     components]))

(defn wire-io!
  "Walks through the workflow vectors and wire up the system workflow
  based on desciption given by each vector.

  System's workflow is a vector of vectors. Each vector contains two
  mandatory element which are:
    * The name of the output component
    * The name of the input component(defn make-splitter
  [components from]
  (spltr/make-splitter (hcomp/output from)))

  and an optional predicate function. This function connects the
  output stream of output component to input stream of input component,
  and in case of existance of a predicate function, it only sends those
  messages which pass the predicate."
  [state ^IPersistentMap components ^IPersistentMap workflow]
  (doseq [splitter (first (reduce connect-workflow state workflow))]
    (impl/commit splitter)))


(defn ^IPersistentMap setup
  "Sets up the workflow of the system by wiring the io of each component
  in the order provided by the user in `:workflow` key."
  [^IPersistentMap system]
  (let [workflow-vector (get-workflow system)]
    (when (not (empty? workflow-vector))
      (do
        (log/debug "Setting up workflow...")
        (wire-io! (utils/get-components system)
                  workflow-vector)
        (log/info "Workflow setup done.")))))
