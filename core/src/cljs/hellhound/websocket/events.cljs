(ns hellhound.websocket.events
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-fx
 :websocket
 (fn [{:keys [:connect :send :disconnect]}]))
