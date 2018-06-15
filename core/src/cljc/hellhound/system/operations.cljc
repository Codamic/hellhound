(ns hellhound.system.operations
   "System operations are funtions which can be used on workflow.")

(def
  ^{:doc "A map which describes a set of operations that should apply to values
          from a source channel before putting them on any sink channel."}

  default-operations
  {;; If the function returns a treuthy by passing the value from the source,
   ;; we will the the value to the sink assigned to this map.
   :filter-fn #(identity %)
   ;; This function will apply to the value came from the source channel before
   ;; sending it to the sink channel.
   :map-fn    #(identity %)})

(defn make-ops-map
  [filter-fn map-fn]
  {:filter-fn (or filter-fn #(identity %))
   :map-fn    (or map-fn    #(identity %))})
