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
   [hellhound.components.impl.persistent-map :as component-impl]
   [hellhound.async :as async]
   [hellhound.logger :as logger]))


;; Public Functions ----------------------------------------

;; IMPORTANT NOTE: These are shortcut functions for public
;; API of HellHound. As a contributor make sure to use the
;; protocol instead.
(def initialize impl/initialize)

(def start! impl/start!)

(def stop! impl/stop!)

(def started? impl/started?)

(def dependencies impl/dependencies)

(def input impl/input)

(def output impl/output)

(defn default-start-fn
  [this ctx]
  (assoc this :context ctx))

(def default-stop-fn identity)


(defn make-component
  "A helper function to create a component map with the given details.

  Returns a component map with the given, `start-fn`, `stop-fn` and
  the optional `f` and `dependencies` collection. The function `f` get
  executed everytime a value is available on the component's input stream."
  {:added      1.0
   :public-api true}
  ([start-fn stop-fn]
   (make-component start-fn stop-fn nil []))
  ([start-fn stop-fn f]
   (make-component start-fn stop-fn f []))
  ([start-fn stop-fn f dependencies]
   {::start-fn start-fn
    ::stop-fn stop-fn
    ::depends-on dependencies
    ::fn f}))


(defn make-transformer
  "Returns a new function which takes a `component` and applies
  the given function `f` to every value received from the input
  stream of the `component` and send the return value of the
  applied function to the output stream of `component`.

  `f` shouldn't block."
  [f]
  (fn [component]
      (streams/consume
       (fn [v]
         (let [processed-v (f component v)]
           (if processed-v
             (streams/>> (output component) processed-v))))
       (input component))))


(defn make-transformer!
  "Returns a new function which takes a `component` and applies
  the given function `f` to every value received from the input
  stream of the `component` and send the return value of the
  applied function to the output stream of `component`.

  `f` can have blocking operations."
  [f]
  (fn [component]
      (streams/consume!
       (fn [v]
         (let [processed-v (f component v)]
           (if processed-v
             (streams/>> (output component) processed-v))))
       (input component))))


(defmacro deftransform
  "Creates a [transform] component with the given `component-name` and uses
  the given `body` to create a function to be used in the main function of
  the component which gets two arguments, the component itself and a value
  fetched from the input stream."
  [component-name & body]
  `(def ~component-name
     {:hellhound.component/start-fn   hellhound.component/default-start-fn
      :hellhound.component/stop-fn    hellhound.component/default-stop-fn
      :hellhound.component/depends-on []
      :hellhound.component/fn
      #(hellhound.component/make-transformer
        (fn [v#]
          (let [f# ~(list* `fn body)
                processed-v# (f# % v#)]
            (when processed-v#
              (hellhound.streams/>> (hellhound.component/output %)
                                    processed-v#)))))}))

(defmacro deftransform!
  [component-name & body]
  `(def ~component-name
     {:hellhound.component/start-fn   hellhound.component/default-start-fn
      :hellhound.component/stop-fn    hellhound.component/default-stop-fn
      :hellhound.component/depends-on []
      :hellhound.component/fn
      #(hellhound.component/make-transformer!
        (fn [v#]
          (let [f# ~(list* `fn body)
                processed-v# (f# % v#)]
            (when processed-v#
              (hellhound.streams/>> (hellhound.component/output %)
                                    processed-v#)))))}))


;; Helpers -------------------------------------------------
(defn get-config
  [component]
  (:config (:context component)))

;; IO helpers ----------------------------------------------
(defn io
  "Returns a vector containing the IO streams of the given `component`.

  The return vector is in the following format: [input-stream, output-stream]."
  [component]
  [(impl/input component) (impl/output component)])


(defn- in->out
  [component consume-fn putfn f]
  (let [[input output] (io component)]
    (consume-fn
        (fn [v]
          (let [processed-v (f v)]
            (when processed-v
              (putfn output processed-v))))
        input)))

(defn input->output
  [component f]
  (in->out component
           hellhound.streams/consume
           streams/>>
           f))


(defn input->output!
  [component f]
  (in->out component
           hellhound.streams/consume!
           streams/>>!
           f))


(defn ->output
  [component v]
  (let [[_ out] (io component)]
    (streams/>> out v)))


(defn ->output!
  [component v]
  (let [[_ out] (io component)]
    (streams/>>! out v)))


;; IO & Execution helpers ----------------------------------
(defn scheduled-output
  [component delay f]
  (let [[_ out] (io component)]
    (async/schedule-fixedrate-interval delay
                                       #(streams/>> out (f)))))
