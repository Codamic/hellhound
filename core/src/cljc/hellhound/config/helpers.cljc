(ns hellhound.config.helpers
  "Several helper functions to make life easier when using then
  hellhound configuration."
  (:require
   [hellhound.config.defaults :as default]))

(defn default-value-for
  "Returns the delay which resolves to the default config value
  of the given key-names."
  [^clojure.lang.PersistentVector key-names]
  (get-in default/config key-names))

(defn ^clojure.lang.Delay derefiy
  "Wrap the given `value` with a delay if it wasn't already wrapped"
  [value]
  (if (delay? value)
    value
    (delay value)))
