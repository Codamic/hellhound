(ns hellhound.http.utils)


(defn uuid
  []
  (str (java.util.UUID/randomUUID)))
