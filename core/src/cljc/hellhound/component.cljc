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
   [hellhound.streams :as streams]
   [hellhound.components.protocols :as impl]
   [hellhound.components.impl.persistent-map :as component-impl]))


;; Public Functions ----------------------------------------

;; IMPORTANT NOTE: These are shortcut functions for public
;; API of HellHound. As a contributor make sure to use the
;; protocol instead.
(def initialize impl/initialize)

(def start! impl/start!)

(def stop! impl/stop!)

(def started? impl/started?)

(def get-name impl/get-name)

(def dependencies impl/dependencies)

(def input impl/input)

(def output impl/output)

(defn default-start-fn
  [this ctx]
  (assoc this :context ctx))

(defn default-stop-fn
  [this]
  this)

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


(defmacro defcomponent
  "Creates a component with the given `component-name` and use the
  given `body` as the main function of the component.

  The defined component will use `hellhound.components.defaults/start-fn` and
  `hellhound.components.defaults/stop-fn` as the `start-fn` and `stop-fn`
  which basically do nothing (`start-fn` attaches the context to the component)."
  [component-name & body]
  `(def ~component-name
     {:hellhound.component/name       (keyword (str *ns*) ~(str component-name))
      :hellhound.component/start-fn   hellhound.components.defaults/start-fn
      :hellhound.component/stop-fn    hellhound.components.defaults/stop-fn
      :hellhound.component/depends-on []
      :hellhound.component/fn         ~(list* `fn body)}))


(defmacro deftransform
  "Creates a [transform] component with the given `component-name` and uses
  the given `body` to create a function to be used in the main function of
  the component which gets two arguments, the component itself and a value
  fetched from the input stream."
  [component-name & body]
  `(def ~component-name
     {:hellhound.component/name       (keyword (str *ns*) ~(str component-name))
      :hellhound.component/start-fn   hellhound.component/default-start-fn
      :hellhound.component/stop-fn    hellhound.component/default-stop-fn
      :hellhound.component/depends-on []
      :hellhound.component/fn
      (fn [component#]
        (let [f# ~(list* `fn body)]
          (hellhound.streams/consume
           (fn [v#]
             (let [processed-v# (f# component# v#)]
               (when processed-v#
                 (hellhound.streams/>> (hellhound.component/output component#)
                                       processed-v#))))
           (hellhound.component/input component#))))}))


(defmacro deftransform!
  [component-name & body]
  `(def ~component-name
     {:hellhound.component/name (keyword (str *ns*) ~(str component-name))
      :hellhound.component/start-fn   hellhound.component/default-start-fn
      :hellhound.component/stop-fn    hellhound.component/default-stop-fn
      :hellhound.component/depends-on []
      :hellhound.component/fn
      (fn [component#]
       (let [f# ~(list* `fn body)]
         (hellhound.streams/consume
          (fn [v#]
            (let [processed-v# (f# component# v#)]
              (when processed-v#
                (hellhound.streams/>>! (hellhound.component/output component#)
                                       processed-v#))))
          (hellhound.component/input component#))))}))


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
              (putfn output processed-v)))))))

(defn input->output
  [component f]
  (in->out component
           streams/<<
           streams/>>
           f))


(defn input->output!
  [component f]
  (in->out component
           streams/<<!
           streams/>>!
           f))
