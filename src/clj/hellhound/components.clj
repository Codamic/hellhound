(ns hellhound.components
  "Main namespace of **Hellhound**'s component system. All the necessary
  functions for creating new component are here. In order to get any
  specific component you need to checkout there ns directly under
  `hellhound.components.component-name` for example for websocket component
  you need to look into `hellhound.components.websocket`"
  (:require
   [clojure.spec.alpha            :as spec]
   [hellhound.components.specs    :as comp-specs]))

(defn- merge-into
  [coll keyname key-spec value]
  (if (empty? value)
    coll
    (merge coll{keyname (spec/conform key-spec
                         value)})))
(defn create-instance
  "Creates an instance map with the given `instance` and `requirements`
  and `inputs`."
  ([instance]
   (create-instance instance [] []))

  ([instance requirements]
   (create-instance instance requirements []))

  ([instance requirements inputs]
   (-> {:instance (spec/conform ::comp-specs/instance instance)}
       (merge-into :requires ::comp-specs/requires (or requirements []))
       (merge-into :inputs   ::comp-specs/inputs (or inputs [])))))
