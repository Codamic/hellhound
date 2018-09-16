(ns hellhound.http.utils
  (:require
   [clojure.java.io :as io]))

(defn- read-file
  [path]
  (slurp (io/resource path)))

(defn html-response
  ([file-path]
   (html-response file-path {}))
  ([file-path {:keys [status headers]}]
   {:status (or status 200)
    :headers (merge {"Content-Type" "text/html"}
                    headers)
    :body (read-file file-path)}))
