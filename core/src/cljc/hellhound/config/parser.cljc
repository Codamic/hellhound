(ns hellhound.config.parser
  "Parser functions for hellhound edn configuration files
  live in this namespace. This namespace contains several
  helpers functions to load a `edn`  configuration file from
  classpath.

  ## why not to use `environ` and `project.clj`?
  Because `environ` convert the values to string and also it's hard
  to fit a hash-map into a environment variable in production."
  #?(:clj
     (:require
      [clojure.edn     :as edn]
      [clojure.java.io :as io])

     :cljs
     (:require
      [cljs.edn :as edn])))


(defn var-reader
  "Reader function for `#hh/var` edn tag which resolve the given
  string as a var and returns the value related to that var."
  [value]
  (let [[namespace var-symbol] (map #(symbol %)
                                    (clojure.string/split value #"/"))]
    (delay
     (let [resolved-symbol (ns-resolve namespace var-symbol)]
       (if-not (nil? resolved-symbol)
         @resolved-symbol
         nil)))))

(def readers
  {'hh/var var-reader})

(defn read-config
  "Read the content of the config file with the given `config-name`
  and return a clojure data structure."
  [config-name]
  (let [resource (io/resource config-name)]
    (if (nil? resource)
      {}
      (edn/read-string {:readers readers} (slurp resource)))))
