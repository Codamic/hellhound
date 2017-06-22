(ns hellhound.config
  "This namespace contains several helpers functions to load a `edn`
  configuration file from classpath.

  ## why not to use `environ` and `project.clj`?
  Because `environ` convert the values to string and also it's hard
  to fit a hash-map into a environment variable in production."
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]))

(def ^{:doc "Default configuration hash-map of the hellhound application.
check out key values of `:keys-doc` meta key."}
  ^{:keys-doc1
    {:http-host "The default hostname or ip address to be use as
                    webserver address (default: localhost)"
     :http-port "The port number for the web server. (default: 3000"}}


  default-config

  {:http-host "localhost"
   :http-port 3000})

(defn read-config
  "Read the content of the config file with the given `config-name`
  and return a clojure data structure."
  [config-name]
  (let [resource (io/resource config-name)]
    (if (nil? resource)
      (throw (ex-info
              (format "Can't find the '%s' config file" config-name)
              {})))
    (edn/read-string (slurp resource))))
