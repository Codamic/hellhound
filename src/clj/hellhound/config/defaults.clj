(ns hellhound.config.defaults
  "This namespace contains default values for **HellHound** configuration")

(def
  ^{:doc "Default configuration hash-map of the hellhound application.
check out key values of `:keys-doc` meta key."}

  ^{:keys-doc1
    {:http-host "The default hostname or ip address to be use as
                    webserver address (default: localhost)"
     :http-port "The port number for the web server. (default: 3000"
     :public-files-path "Path to the directory in `resources` which contains
                         public files such as images,css,js,etc."}}

  config

  {:http-host         "localhost"
   :http-port         3000
   :public-files-path "public"})
