(ns hellhound.components.impl.persistent-map
  {:added 1.0}
  (:require
   [clojure.spec.alpha     :as s]
   [clojure.spec.gen.alpha :as gen]
   [hellhound.streams :as stream]
   [hellhound.core    :as core]
   [hellhound.logger  :as log]
   [hellhound.components.protocols :as protocol]
   [hellhound.system :as sys]))

;; Private Functions ---------------------------------------
;; These functions are the actual implementation of IComponent
;; Protocol for IPersistentMap.

(defn- initialize-component
  "This function is responsible to initialize the given `component` by
  initializing the input and ouput manifolds of the component."
  [component]
  (if (true? (:hellhound.component/initialized? component))
    ;; Return the component if it already initialized.
    component
    (let [default-io-buffer-size (sys/get-config [:components-io-buffer-size])

          default-stream-fn      #(stream/stream default-io-buffer-size)
          input-stream-fn        (get component
                                      :input-stream-fn
                                      default-stream-fn)
          output-stream-fn       (get component
                                      :output-stream-fn
                                      default-stream-fn)]

      (assert default-io-buffer-size)
      (assoc component
             :hellhound.component/initialized? true
             :hellhound.component/started?     false
             :hellhound.component/input        (input-stream-fn)
             :hellhound.component/output       (output-stream-fn)))))


(defn- start-component!
  "Fetches and calls the `start-fn` of the given `component`.

  This function assigns the `started?` key to `true` on the return
  value of `start-fn` which should be a valid component. `started?`
  key basically demonstrates that the component in question is running."
  [component context]
  (let [initialized-component (protocol/initialize component)
        start-fn              (:hellhound.component/start-fn initialized-component)
        started?              (protocol/started? initialized-component)]

    (if-not started?
      (do
        (log/debug (format "Starting component '%s'..."
                           (protocol/get-name component)))
        (assoc (start-fn initialized-component context)
               :hellhound.component/started?
               true))

      (do (log/debug (format "Component '%s' already started. Skipping..."
                             (protocol/get-name initialized-component)))
          initialized-component))))


(defn- stop-component!
  "Fetches and calls the `stop-fn` of the given `component`.

  This function assigns the `started?` key to `false` on the return
  value of `start-fn` which should be a valid component. Falsy value for
  `started?` demonstrates that the component in question is not running."
  [component]
  (let [stop-fn (:hellhound.component/stop-fn component)]
      (if (protocol/started? component)
        (do (log/debug (format "Stopping '%s' component ..."
                               (protocol/get-name component)))
            (assoc (stop-fn component)
                   :hellhound.component/started?
                   false))
        (do (log/debug (format "Skipping '%s' already stopped..."
                               (protocol/get-name component)))
            component))))


(defn- component-started?
  "Returns `true` if the given component is `started?`."
  [component]
  (or (:hellhound.component/started? component) false))


(defn- name-of
  "Returns the `name` of the given `component`."
  [component]
  (:hellhound.component/name component))


(defn- dependencies-of
  "Returns a collection of dependencies of the given `component`."
  [component]
  (:hellhound.component/depends-on component))


(defn- input-of
  "Returns the input manifold of the given `component`.

  The input of the component is a manifold whichis going to receive
  the incoming dataflow from the output of the component upstream."
  [component]
  (let [new-component (protocol/initialize component)]
    (assert (:hellhound.component/input new-component)
            "::input should not be empty. Please file a bug")
    (:hellhound.component/input new-component)))


(defn- output-of
  "Returns the output manifold of the given `component`.

  The output of the component is a manifold which should
  flow the output data of the component to the downstream
  component."
  [component]
  (let [new-component (protocol/initialize component)]
    (assert (:hellhound.component/output new-component)
            "::output should not be empty. Please file a bug")
    (:hellhound.component/output new-component)))


(defn- get-fn
  [component]
  (:hellhound.component/fn component))


(defn- ready?
  [component]
  (and (protocol/started? component)
       (:hellhound.component/fn-called? component)))


(defn- mark-as-ready
  [component]
  (assoc component
         :hellhound.component/fn-called?
         true))


;; IComponent Implementations ------------------------------
(extend-protocol protocol/IComponent
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
    (output-of component))

  (get-fn [component]
    (get-fn component))

  (ready? [component]
    (ready? component))

  (mark-as-ready [component]
    (mark-as-ready component)))


;; SPECS ---------------------------------------------------
(s/def :hellhound.component/name qualified-keyword?)
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
(s/def :hellhound.component/start-fn
  (s/with-gen
    fn?
    #(s/gen #{(fn [component context] component)})))

(s/def :hellhound.component/stop-fn
  (s/with-gen
    fn?
    #(s/gen #{(fn [component context] component)})))

(s/def :hellhound.component/stream
  (s/with-gen stream/stream?
    #(s/gen #{(stream/stream) (stream/stream 100)})))

(s/def :hellhound.component/input-stream-fn
  (s/fspec :args (s/cat) :ret :hellhound.component/stream))

(s/def :hellhound.component/output-stream-fn :hellhound.component/input-stream-fn)

(s/def :hellhound.component/depends-on (s/coll-of keyword? :kind vector? :distinct true))
(s/def :hellhound.component/component (s/keys :req [:hellhound.component/name
                                                    :hellhound.component/start-fn
                                                    :hellhound.component/stop-fn]
                                              :opt [:hellhound.component/depends-on
                                                    :hellhound.component/input-stream-fn
                                                    :hellhound.component/output-stream-fn]))
