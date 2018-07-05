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
  (:require
   [hellhound.components.impl.persistant-map]
   [hellhound.streams :as streams]
   [hellhound.component.protocols :as impl]))


;; Public Functions ----------------------------------------

(def initialize impl/initialize)

(def start! impl/start!)

(def stop! impl/stop!)

(def started? impl/started?)

(def get-name impl/get-name)

(def dependencies impl/dependencies)

(def input impl/input)

(def output impl/output)

(def executor impl/executor)

(defn make-component
  "A short cut function to create a component map with the given details.

  Returns a component map with the given `name`, `start-fn`, `stop-fn` and
  the optional `dependencies` collection."
  {:added      1.0
   :public-api true}
  ([component-name start-fn stop-fn]
   (make-component component-name start-fn stop-fn []))

  ([component-name start-fn stop-fn dependencies]
   {::name component-name
    ::start-fn start-fn
    ::stop-fn stop-fn
    ::depends-on dependencies}))

(defn io
  "Returns a vector containing the IO streams of the given `component`.

  The return vector is in the following format: [input-stream, output-stream]."
  [component]
  [(impl/input component) (impl/output component)])


(defn- in->out
  [component takefn putfn f]
  (let [[input output] (io component)]
    (takefn input
        (fn [v]
          (let [processed-v (f v)]
            (when processed-v
              (putfn ouput processed-v)))))))

(defn input->output
  [component f]
  (in->out componet
           streams/<<
           streams/>>
           f))


(defn input->output!
  [component f]
  (in->out componet
           streams/<<!
           streams/>>!
           f))
