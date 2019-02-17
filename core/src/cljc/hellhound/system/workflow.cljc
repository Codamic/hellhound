(ns hellhound.system.workflow
  "System's workflow a vector describing the dataflow of the system.
  Each components has an input and an output stream. Each stream is a
  `hellhound.stream/stram`. HellHound connects io of each component
  to another component based on the desciption given by the `:workflow`
  of the system.

  System's workflow is a vector of hashmaps. Each hashmap contains two
  mandatory keys which are:

    * `:hellhound.workflow/source` which its value should be name of
      the output component
    * `:hellhound.workflow/sink`  which its value should be name of
      the input component

  and an optional `:hellhound.workflow/predicate` key that its value
  should be predicate function.

  Hellhound setup up a connection based on every hashmap (Node) and
  connects the output of the source to input of the sink.

  predicate function should return a boolean which filters all the
  messages (See `hellhound.message`) from source component and feed
  them to the sink component.

  Predicate function must be pure and free of side effects."
  (:require
   [hellhound.components.protocols :as cimpl]
   [hellhound.system.protocols     :as impl]
   [hellhound.system.impl.splitter :as spltr]
   [hellhound.logger               :as log]))


(defn- invalid-workflow
  [component node]
  (throw (ex-info (format "Invalid component '%s' in workflow."
                          (cimpl/get-name component))
                  {:cause component
                   :node  node})))


(defn- invalid-component-name
  [cname node]
  (throw (ex-info
          (format "Can't find component '%s' in the system." cname)
          {:cause cname
           :node  node})))


(defn parse-node
  [node]
  [(:hellhound.workflow/source node)
   (:hellhound.workflow/sink node)])


(defn make-splitter
  "Creates a splitter from the given `source-component` component."
  [source-component]
  (spltr/output-splitter (cimpl/output source-component)))


(defn get-node-components
  "Return a vector containing the source component of the given
  `node` in the given system as the first element and the sink
  component as the second element."
  [system node]
  (let [[source-name sink-name] (parse-node node)
        source (impl/get-component system source-name)
        sink   (impl/get-component system sink-name)]
    ;; Validates the source and sink components
    (when (nil? source)
      (invalid-component-name source node))

    (when (nil? sink)
      (invalid-component-name sink node))
    [source sink]))


(defn connect-node
  "Returns a reduce function for the given `system` which reduce over
  workflow nodes and setup their connection using the source compoennt
  as the root of each splitter."
  [system]
  (fn [splitters  node]
    (let [[source sink] (get-node-components system node)
          ;; Get or create a new splitter from the source
          ;; component
          splitter (or (get splitters (cimpl/get-name source))
                       (make-splitter source))]

      (impl/connect splitter (cimpl/input sink) node)
      (assoc splitters (cimpl/get-name source) splitter))))


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
  [system workflow]
  (let [splitters (reduce (connect-node system) {}  workflow)
        commit-fn (fn [acc [k v]]
                    (assoc acc k (impl/commit v)))]
    (doall (reduce commit-fn {} splitters))))


(defn wire-components
  [system workflow-vector]
  (if-not (empty? workflow-vector)
    ;; We didn't move :splitters to a protocol function
    ;; for system because it's a workflow thing only and
    ;; update-system would handle it for us.
    (impl/update-system system
                        :splitters
                        (wire-io system workflow-vector))
    (do
      (log/warn "':workflow' of the system is empty. Skipping....")
      system)))


(defn run-component-fn
  "Runs the main function of the given component if the component is not
  ready and marks it as ready. In both cases it will returns the component."
  [component]
  (if-not (cimpl/ready? component)
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
  "Runs the main function of the source and sink components of
  the given node (nodes in the workflow vector of the given system)."
  [system node]
  (let [[source-name sink-name] (parse-node node)]
    ;; NOTE: There is not need for validating the source and sink names
    ;;       because at this stage we already validated the workflow.

    ;; Run main function of source and sink components and updated
    ;; the system with the ready components.
    (reduce
     #(impl/update-component %1 %2 (run-component-fn (impl/get-component %1 %2)))
     system
     [source-name sink-name])))


(defn setup-main-functions
  "Walks through the workflow and runs main function of all the components
  and returns the updated system."
  [system workflow]
  (reduce setup-pipe-main-fns system workflow))


(defn setup
  "Sets up the workflow of the system by wiring the io of each component
  in the order provided by the user in `:workflow` key."
  [system]
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
  (close-splitters! system))

;; Helper Macros/Funcitons ----------------------------------------------------
;; (defmacro >
;;   [workflows]
;;   (map #(workflow->hashmap %) workflow))
