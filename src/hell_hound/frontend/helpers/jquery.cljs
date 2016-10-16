(ns hell-hound.frontend.helpers.jquery
  (:require [re-frame.core :as r]))


(defn $
  "Simple function to wrap jquery by dispatching jquery event."
  [selector method & values]
  ;; (let [elem (js/$ (clj->js selector))
  ;;        method (clj->js method)]
  ;;    (.call (aget elem  method) elem values))))
  (let [args (vec (concat [:jquery selector method] values))]
    (apply r/dispatch [args])))
