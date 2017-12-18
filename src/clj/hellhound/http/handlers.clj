(ns hellhound.http.handlers)

(defn not-found
  [req]
  {:status 404 :headers {} :body "NOT FOUND!"})

(defn hello
  [req]
  {:status 200
   :headers []
   :body "Welcome to HellHound"})

(defn default-handler
  [req]
  {:status 200
   :headers []})
