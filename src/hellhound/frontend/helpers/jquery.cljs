(ns hellhound.frontend.helpers.jquery
  (:require [re-frame.core :as r]))


(defn $
  "Simple function to wrap jquery by dispatching jquery event."
  [selector method & values]
  (let [args (vec (concat [:jquery selector method] values))]
    (apply r/dispatch [args])))
