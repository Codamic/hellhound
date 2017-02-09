(ns hellhound.ring.response
  "This namespace contains several helper function to
  help users to produce a ring response."

  (:require [ring.util.response :as res]
            [selmer.parser      :as parser]))


(defn- content-type-of
  [res-format]
  (when-not (nil? res-format)
    (cond
      (= res-format :json) "application/json"
      (= res-format :xml)  "application/xml"
      (= res-format :html) "text/html"
      :else (str res-format))))

(defn respond-with
  "A shortcut function to render a template given by the `template-path`
  with the given `context`.

  `options` contains only two key. `status` and `format`."
  ([template-path context]
   (respond-with template-path context {}))
  ([template-path context options]
   (let [{:keys [status format]} options
         status-code             (or status 200)
         content-type            (or format :html)
         template                (parser/render-file template-path context)
         body                    (res/response template)
         response                (res/content-type body (content-type-of content-type))
         complete-response       (assoc-in response [:status] status-code)]
     complete-response)))
