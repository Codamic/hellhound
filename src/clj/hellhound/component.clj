(ns hellhound.component
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]
            [manifold.stream        :as stream]
            [hellhound.core :as core]
            [hellhound.logger :as log])
  (:import (clojure.lang PersistentArrayMap)))


;; Protocols
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


(extend-type PersistentArrayMap
  IComponent
  (initialize [component]
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


  (start! [component context]
    (let [start-fn (::start-fn component)]
      (if (not (started? component))
        (do
          (log/debug "Starting '" (::name component) "' component...")
          (assoc (start-fn component context) ::started? true))

        (do
          (log/debug "Component '" (::name component) "' already started. Skipping...")
          component))))

  (stop! [component]
    (let [stop-fn (::stop-fn component)]
      (if (started? component)
        (assoc (stop-fn component) ::started? false)
        component)))

  (started? [component]
    (or (::started? component) false))

  (get-name [component]
    (::name component))

  (dependencies [component]
    (::depends-on component))

  (input [component]
    ;; TODO: Do we need to return a new stream if input was nil ?
    (::input component))

  (output [component]
    (::output component)))

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
;;   (s/fspec :args (s/cat :_ map? :context map?)
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
