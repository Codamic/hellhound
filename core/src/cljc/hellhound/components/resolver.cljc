(ns hellhound.components.resolver
  [:require
   [hellhound.message :as msg]
   [hellhound.component :refer [deftransform]]])


(deftransform resolve
  [this message]
  (msg/resolve! message))
