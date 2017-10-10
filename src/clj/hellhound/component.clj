(ns hellhound.component
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]
            [manifold.stream        :as stream]
            [hellhound.core :as core]
            [hellhound.logger :as log])
  (:import (clojure.lang PersistentArrayMap
                         PersistentHashMap)))


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
  [component]
  (let [default-io-buffer-size (core/get-config :components :io-buffer-size)
          default-stream-fn #(stream/stream default-io-buffer-size)
          input-stream-fn   (get component
                                 :input-stream-fn
                                 default-stream-fn)
          output-stream-fn  (get component
                                 :output-stream-fn
                                 default-stream-fn)]
      (assert default-io-buffer-size)
      (assoc component
             ::started? false
             ::input    (input-stream-fn)
             ::output   (output-stream-fn))))

(defn- start-component!
  [component context]
  (let [start-fn (::start-fn component)]
      (if (not (started? component))
        (do
          (log/debug "Starting '" (::name component) "' component...")
          (assoc (start-fn component context) ::started? true))

        (do
          (log/debug "Component '" (::name component) "' already started. Skipping...")
          component))))

(defn- stop-component!
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
  [component]
  (or (::started? component) false))

(defn- name-of
  [component]
  (::name component))

(defn- dependencies-of
  [component]
  (::depends-on component))

(defn- input-of
  [component]
  (assert (::input component) "::input should not be empty. Please file a bug")
  (::input component))

(defn- output-of
  [component]
  (assert (::output component)
          "::input should not be empty. Please file a bug")
  (::output component))


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
;;              :fn #(s/valid? ::component (:ret %)))
;;     #(s/gen #{(fn [component context] component)})))

;; (s/def ::stop-fn
;;   (s/with-gen
;;     (s/fspec :args (s/cat :_ map?)
;;              :ret map?
;;              :fn #(s/valid? ::component (:ret %)))
;;     #(s/gen #{(fn [component] component)})))


;; (s/def ::start-fn
;;   (s/fspec :args (s/cat :_ map? :_ map?)
;;            :ret map?
;;            :fn map?))

;; (s/def ::stop-fn
;;   (s/fspec :args (s/cat :_ map?)
;;            :ret map?
;;            :fn map?))



(s/def ::start-fn fn?)
(s/def ::stop-fn fn?)

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
