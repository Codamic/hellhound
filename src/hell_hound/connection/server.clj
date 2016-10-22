(ns hell-hound.connection.server
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [compojure.core :as compojure :refer [GET POST]]))



(let [{:keys [ch-recv
              send-fn
              connected-uids
              ajax-post-fn
              ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {:packer :edn})]

  (def ring-ajax-post  ajax-post-fn)
  (def ring-handshake  ajax-get-or-ws-handshake-fn)

  ; ChannelSocket's receive channel
  (def ch-chsk         ch-recv)

  ; ChannelSocket's send API fn
  (def chsk-send!      send-fn)

  ; Watchable, read-only atom
  (def connected-uids  connected-uids))



(defn routes
  "Routes macro allows developer to setup sente connection and urls easy
  and painlessly."
  []
  (compojure/routes
   (compojure/context "/hellhound" []
                      (compojure/GET  "/" req (ring-handshake req))
                      (compojure/POST "/" req (ring-ajax-post req)))))
