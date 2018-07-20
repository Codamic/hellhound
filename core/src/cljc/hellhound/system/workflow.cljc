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
  (:require
   [hellhound.logger               :as log]
   [hellhound.components.protocols :as cimpl]
   [hellhound.streams              :as streams]
   [hellhound.system.impl.splitter :as spltr]
   [hellhound.system.operations    :as op]
   [hellhound.system.impl.system   :as sys]
   [hellhound.system.protocols     :as impl]
   [hellhound.system.utils         :as utils])


  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn- invalid-workflow
  [component]
  (throw (ex-info (format "Invalid component '%s' in workflow."
                          (cimpl/get-name component))
                  {:cause component})))

(defn- invalid-component-name
  [cname]
  (throw (ex-info
          (format "Can't find component '%s' in the system." cname)
          {:cause cname})))

(defn parse
  "Returns a operations map based on the given arguments."
  ([from to]
   (parse from #(identity %) #(identity %) to))

  ([from pred to]
   (parse from pred #(identity %) to))

  ([from pred map-fn to]
   [from (op/make-ops-map pred map-fn) to]))

(defn make-splitter
  "Creates a splitter from the given `source-component` component."
  [source-component]
  (spltr/output-splitter (cimpl/output source-component)))

(defn connect-workflow
  "Setup and connect the components through the splitters."
  [[splitters components] connection-vec]

  (let [[from ops-map to] (apply parse connection-vec)
        source-component  (get components from)
        dest-component    (get components to)]

    ;; Validates the source and dest components
    (when (nil? source-component)
      (invalid-component-name from))

    (when (nil? dest-component)
      (invalid-component-name to))


    ;; Get or create a new splitter from the source
    ;; component
    (let [splitter (or (get splitters from)
                       (make-splitter source-component))]

      (impl/connect splitter
                    (cimpl/input dest-component)
                    ops-map)

      [(assoc splitters from splitter)
       components])))


(defn wire-io
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
  [components workflow]
  (let [[splitters _] (reduce connect-workflow [{} components] workflow)
        commit-fn     (fn [acc [k v]]
                        (assoc acc k (impl/commit v)))]
    (doall (reduce commit-fn {} splitters))))

(defn wire-components
  [system workflow-vector]
  (if (not (empty? workflow-vector))
    ;; We didn't move :splitters to a protocol function
    ;; for system because it's a workflow thing only and
    ;; update-system would handle it for us.
    (impl/update-system system
                        :splitters
                        (wire-io (impl/components-map system)
                                 workflow-vector))
    (do
      (log/warn "':workflow' of the system is empty. Skipping....")
      system)))

(defn run-component-fn
  "Runs the main function of the given component if the component is not
  ready and marks it as ready. In both cases it will returns the component."
  [component]
  (if (not (cimpl/ready? component))
    (let [f (cimpl/get-fn component)]
      (if f
        (f component)
        ;; For now we need to warn the user of missing main function until
        ;; we reach a concrete decision about what should we do in this
        ;; situation. Because it make sense for a producer component
        ;; to not to have a main function and do all it's job in `start-fn`.
        (log/debug (format "Skipping '%s' component. No consumer function."
                           (cimpl/get-name component))))
      ;; Even if the component does not have a main function
      ;; we need to mark it as ready because we already processed
      ;; the component.
      (cimpl/mark-as-ready component))
    component))


(defn setup-pipe-main-fns
  "Runs the main function of the source and destination components of
  the given pipe (pipe iin the given system."
  [system pipe]
  (let [src-name (first pipe)
        dst-name (last pipe)]

    ;; NOTE: There is not need for validating the source and destination names
    ;;       because at this stage we already validated the workflow.

    ;; Run main function of source and destination components and updated
    ;; the system with the ready components.
    (reduce
     #(impl/update-component %1 %2 (run-component-fn (impl/get-component %1 %2)))
     system
     [src-name dst-name])))


(defn setup-main-functions
  "Walks through the workflow and runs main function of all the components
  and returns the updated system."
  [system workflow]
  (reduce setup-pipe-main-fns system workflow))


(defn ^IPersistentMap setup
  "Sets up the workflow of the system by wiring the io of each component
  in the order provided by the user in `:workflow` key."
  [^IPersistentMap system]
  (log/debug "Setting up the system workflow...")
  (let [workflow-vector (impl/get-workflow system)
        wired-system    (wire-components system workflow-vector)
        ready-system    (setup-main-functions wired-system workflow-vector)]
    (log/debug "Workflow setup has been done.")
    ready-system))


;; Tear down process ----------------------------------------------------------

(defn close-splitters!
  [system]
  (doseq [splitter (vals (:splitters system))]
    (impl/close! splitter))
  (dissoc system :splitters))


(defn teardown
  [system]
  (log/debug "Tearing down the system workflow...")
  (-> system
      (close-splitters!)))
