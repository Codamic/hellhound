(ns hellhound.component
  "Components are the main parts of HellHound systems. Basically
  each components should be an implementation of `IComponent`. The
  protocol which defines a component functionality. By default HellHound
  implements `IComponent` protocols for hashmaps only. So we can define
  components in form of maps.

  In order to define a component a map should should contains following
  keys (All the keys should be namespaced keyword under `hellhound.component`):

  * `name`: The name of the component. It should be a namespaced keyword.
  This key is **mandatory**.

  * `start-fn`: A function which takes the component map as the only argument
  and return the component with the necessary keys attached to it. This
  function is responsible for **starting** the component.
  This key is **mandatory**.

  * `stop-fn`: A function which takes the component map as the only argument
  and return the component with the necessary keys attached to it. This
  function is responsible for **stoping** the component.
  This key is **mandatory**.

  * `depends-on`: This key specifies all the components which are the
   dependencies of the current component. A collection  of components
  name.
  This key is optional.

  * `input-stream-fn`: A function which returns a `manifold` as the input
  of the component. You rarely need to use this key for a component.
  This key optional.

  * `output-stream-fn`: A function which returns a `manifold` as the output
  of the component. You rarely need to use this key for a component.
  This key optional.

  So as an example:

  ```clojure
  (def sample-component
    {:hellhound.component/name :sample.core/component-A
     :hellhound.component/start-fn (fn [component] component)
     :hellhound.component/stop-fn (fn [component] component)
     :hellhound.component/depends-on [:sample.core/component-B]})
  ```

  In this example `start-fn` and `stop-fn` don't do anything.
  "
  {:added 1.0}
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]
            [manifold.stream        :as stream]
            [hellhound.core :as core]
            [hellhound.logger :as log]))

;; Protocols -----------------------------------------------
(defprotocol IComponent
  "This protocol defines a very basic component for hellhound system."
  (initialize [component]
    "Returns the initialized component.")

  (start! [component context]
    "Starts the component.")

  (stop!  [component]
    "Stops the component.")

  (started? [component]
    "Returns a `true` if component started and `false` otherwise.")

  (get-name [component]
    "Returns the name of the component.")

  (dependencies [component]
    "Returns a vector of dependency names.")

  (input [component]
    "Returns the input stream of the component.")

  (output [component]
    "Returns the output stream of the component."))

;; Private Functions ---------------------------------------
;; These functions are the actual implementation of IComponent
;; Protocol for IPersistentMap.
(defn- initialize-component
  "This function is responsible to initialize the given `component` by
  initializing the input and ouput manifolds of the component."
  [component]
  (if (true? (::initialized? component))
    ;; Return the component if it already initialized.
    component
    (let [default-io-buffer-size (core/get-config :components :io-buffer-size)
          default-stream-fn      #(stream/stream default-io-buffer-size)
          input-stream-fn        (get component
                                      :input-stream-fn
                                      default-stream-fn)
          output-stream-fn       (get component
                                      :output-stream-fn
                                      default-stream-fn)]

      (assert default-io-buffer-size)
      (assoc component
             ::initialized? true
             ::started?     false
             ::input        (input-stream-fn)
             ::output       (output-stream-fn)))))

(defn- start-component!
  "Fetches and calls the `start-fn` of the given `component`.

  This function assigns the `started?` key to `true` on the return
  value of `start-fn` which should be a valid component. `started?`
  key basically demonstrates that the component in question is running."
  [component context]
  (let [initialized-component (initialize component)
        start-fn              (::start-fn initialized-component)]
    (if (not (started? initialized-component))
      (do
        (log/debug (format "Starting component '%s'..."
                           (::name component)))
        (assoc (start-fn initialized-component context) ::started? true))

      (do
        (log/debug (format "Component '%s' already started. Skipping..."
                           (::name initialized-component)))
        initialized-component))))

(defn- stop-component!
  "Fetches and calls the `stop-fn` of the given `component`.

  This function assigns the `started?` key to `false` on the return
  value of `start-fn` which should be a valid component. Falsy value for
  `started?` demonstrates that the component in question is not running."
  [component]
  (let [stop-fn (::stop-fn component)]
      (if (started? component)
        (do
          (log/debug (format "Stopping '%s' component ..."
                             (get-name component)))
          (assoc (stop-fn component) ::started? false))
        (do
          (log/debug (format "Skipping '%s' already stopped..."
                             (get-name component)))
          component))))

(defn- component-started?
  "Returns `true` if the given component is `started?`."
  [component]
  (or (::started? component) false))

(defn- name-of
  "Returns the `name` of the given `component`."
  [component]
  (::name component))

(defn- dependencies-of
  "Returns a collection of dependencies of the given `component`."
  [component]
  (::depends-on component))

(defn- input-of
  "Returns the input manifold of the given `component`.

  The input of the component is a manifold whichis going to receive
  the incoming dataflow from the output of the component upstream."
  [component]
  (let [new-component (initialize component)]
    (assert (::input new-component)
            "::input should not be empty. Please file a bug")
    (::input new-component)))

(defn- output-of
  "Returns the output manifold of the given `component`.

  The output of the component is a manifold which should
  flow the output data of the component to the downstream
  component."
  [component]
  (let [new-component (initialize component)]
    (assert (::output new-component)
            "::output should not be empty. Please file a bug")
    (::output new-component)))

;; Public Functions ----------------------------------------
(defn io
  "Returns a vector containing the IO streams of the given `component`.

  The return vector is in the following format: [input-stream, output-stream]."
  [component]
  [(input component) (output component)])

;; IComponent Implementations ------------------------------
(extend-protocol IComponent
  clojure.lang.IPersistentMap
  (initialize [component]
    (initialize-component component))

  (start! [component context]
    (start-component! component context))

  (stop! [component]
    (stop-component! component))

  (started? [component]
    (component-started? component))

  (get-name [component]
    (name-of component))

  (dependencies [component]
    (dependencies-of component))

  (input [component]
    (input-of component))

  (output [component]
    (output-of component)))

;; SPECS ---------------------------------------------------
(s/def ::name qualified-keyword?)
;; (s/def ::start-fn
;;   (s/with-gen
;;     (s/fspec :args (s/cat :_ map? :context map?)
;;              :ret map?
;;              ;; TODO: We need to improve the :fn function to check for
;;              ;; necessary keys
;;              :fn #(map? (:ret %)))
;;     #(s/gen #{(fn [component context] component)})))

;; (s/def ::stop-fn
;;   (s/with-gen
;;     (s/fspec :args (s/cat :_ map?)
;;              :ret map?
;;              ;; TODO: We need to improve the :fn function to check for
;;              ;; necessary keys
;;              :fn #(map? (:ret %)))
;;     #(s/gen #{(fn [component] component)})))
(s/def ::start-fn
  (s/with-gen
    fn?
    #(s/gen #{(fn [component context] component)})))

(s/def ::stop-fn
  (s/with-gen
    fn?
    #(s/gen #{(fn [component context] component)})))

(s/def ::stream
  (s/with-gen stream/stream?
    #(s/gen #{(stream/stream) (stream/stream 100)})))

(s/def ::input-stream-fn
  (s/fspec :args (s/cat) :ret ::stream))

(s/def ::output-stream-fn ::input-stream-fn)

(s/def ::depends-on (s/coll-of keyword? :kind vector? :distinct true))
(s/def ::component (s/keys :req [::name ::start-fn ::stop-fn]
                           :opt [::depends-on
                                 ::input-stream-fn
                                 ::output-stream-fn]))
