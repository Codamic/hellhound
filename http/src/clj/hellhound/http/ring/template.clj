(ns hellhound.http.ring.template
  (:require
   [selmer.parser :as selmer]
   [hellhound.http.ring.response :as res]))


(defn render
  "Renders the template at the given `template-path` using
  the given `context` map and `opts` map."
  ([template-path context]
   (render template-path context {}))

  ([template-path context opts]
   (res/html
    (selmer/render-file template-path context) opts)))
