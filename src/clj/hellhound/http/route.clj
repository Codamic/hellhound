(ns hellhound.http.route
  "HellHound's http router namespace.
  DOCTODO"
  (:require
   [clojure.spec.alpha :as s]
;;   [bidi.bidi          :as bidi]
   [bidi.ring          :as bring]
   [hellhound.core     :as hellhound]))

(defn hello
  [req]
  {:status 200
   :headers []
   :body "Yeah"})

(def routes
  (bring/make-handler
   ["/" {:get {"" hello}}]))

;; (bidi/match-route hellhound-routes "/")
;; (bidi/path-for hellhound-routes :something)
