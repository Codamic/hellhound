(ns hellhound.system.utils
  "TODO"
  (:import (clojure.lang IPersistentMap
                         PersistentVector)))


(defn ^PersistentVector get-components
  "Returns the components catalog of the given `system`."
  [^IPersistentMap system]
  (:components system))
