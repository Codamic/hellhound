(ns hellhound.system.utils
  "TODO"
  (:import (clojure.lang IPersistentMap
                         PersistentVector)))

(defn ^PersistentVector get-components
  "Returns the components catalog of the given `system`."
  [^IPersistentMap system]
  (:components system))


(defn ^PersistentVector get-workflow
  "Returns the workflow desciption of the given `system`."
  [^IPersistentMap system]
  (:workflow system))
