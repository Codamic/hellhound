(ns hellhound.components.specs
  "Spec definitions for `component` system live in here."
  (:require [clojure.spec.alpha             :as s]
            [hellhound.components.protocols :as protocols]))


(s/def ::started? boolean?)
(s/def ::requires (s/coll-of keyword?))
(s/def ::inputs   (s/coll-of keyword?))

(s/def ::instance #(satisfies? protocols/Lifecycle %))

(s/def ::component-map
  (s/keys :req [::instance]
             :opt [::started? ::requires ::inputs]))

(s/def ::components (s/map-of keyword? ::component-map))
