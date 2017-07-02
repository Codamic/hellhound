(ns hellhound.components
  "Main namespace of **Hellhound**'s component system. All the necessary
  functions for creating new component are here. In order to get any
  specific component you need to checkout there ns directly under
  `hellhound.components.component-name` for example for websocket component
  you need to look into `hellhound.components.websocket`"
  (:require
   [clojure.spec.alpha            :as spec]
   [hellhound.components.core     :as core]))

(defn- merge-into
  [coll keyname value]
  (if (empty? value)
    coll
    (merge coll{keyname (spec/conform
                         (keyword "core/" (name keyname))
                         value)})))
(defn create-component
  "Creates a component map with the given `instance` and `requirements`
  and `inputs`."
  ([instance]
   (create-component instance [] []))

  ([instance requirements]
   (create-component instance requirements []))

  ([instance requirements inputs]
   (-> {:instance (spec/conform ::core/instance instance)}
       (merge-into :requires (or requirements []))
       (merge-into :inputs   (or inputs [])))))
