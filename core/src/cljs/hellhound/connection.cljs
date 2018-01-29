(ns hellhound.connection
  "This namespace contains means necessary to connect to the server application
  via websocket and a ajax fallback."
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])

  (:require
   [re-frame.core   :as re-frame]
   [taoensso.encore :as encore     :refer-macros (have have?)]
   [cljs.core.async :as async      :refer (<! >! put! chan)]))
