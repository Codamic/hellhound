(ns hellhound.config
  "This namespace contains several helpers functions to load a `edn`
  configuration file from classpath.

  ## why not to use `environ` and `project.clj`?
  Because `environ` convert the values to string and also it's hard
  to fit a hash-map into a environment variable in production."
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]))

(defn read-config
  "Read the content of the config file with the given `config-name`
  and return a clojure data structure."
  [config-name]
  (edn/read-string (slurp (io/resource config-name))))
