(ns hell-hound.frontend.fx.jquery-interceptor
  (:require [re-frame.core :as r]
            [cljsjs.jquery]))

(r/reg-fx
 :jquery
 (fn [{:keys [method values selector]}]
   (let [elem (js/$ (clj->js selector))
         method (clj->js method)]
     (.call (aget elem  method) elem values))))
