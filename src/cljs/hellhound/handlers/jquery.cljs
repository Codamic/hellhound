(ns hellhound.handlers.jquery
  "JQuery effect is responsible for the :jquery effect.
   The value of the effect should be as follow:

   ```clojure
   { :jquery { :selector \"#some-id\"    ;; The jquery selector
               :method   :slideDown
               :values   400 }}
   ```

   This is the same as calling `$(\"#some-id\").slideDown(400)` in
   javascript."
  (:require [re-frame.core :as r]
            [cljsjs.jquery]))


(r/reg-fx
 :jquery
 (fn [{:keys [method values selector]}]
   (let [elem   (js/$ (clj->js selector))
         method (clj->js method)
         args   (concat [elem] values)]
     (.apply (aget elem method) elem (clj->js values)))))


(r/reg-event-fx
 :jquery
 (fn [_ [_ selector func & values]]
   {:jquery {:method func :values values :selector selector}}))
