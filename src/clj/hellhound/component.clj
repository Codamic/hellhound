(ns hellhound.component
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]
            [manifold.stream        :as stream]
            [hellhound.core :as core])
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
    "Returns a vector of dependency names."))

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
             ::input (input-stream-fn)
             ::outpu (output-stream-fn))))


  (start! [this context]
    (let [start-fn (::start-fn this)]
      (assoc (start-fn this context)
             ::started? true)))
  (stop! [this]
    (let [stop-fn (::stop-fn this)]
      (assoc (stop-fn this) ::started? false)))

  (started? [this]
    (or (::started? this) false))

  (get-name [this]
    (::name this))

  (dependencies [this]
     (::depends-on this)))

;; SPECS ---------------------------------------------------
(s/def ::name qualified-keyword?)
(s/def ::start-fn
  (s/fspec :args (s/cat :this map? :context map?
                        :ret map?
                        :fn #(= (::name (:ret %))
                                (::name (:this (:args %)))))))

(s/def ::stop-fn
  (s/fspec :args (s/cat :this map? :context map?
                        :ret map?
                        :fn #(= (::name (:ret %))
                                (::name (:this (:args %)))))))

(s/def ::stream stream/stream?)

(s/def ::input-stream-fn
  (s/fspec :args (s/cat) :ret ::stream))


(s/def ::output-stream-fn ::input-stream-fn)

(s/def ::depends-on (s/coll-of keyword? :kind vector? :distinct true))
(s/def ::component (s/keys :req [::name ::start-fn ::stop-fn]
                           :opt [::depends-on
                                 ::input-stream-fn
                                 ::output-stream-fn]))
