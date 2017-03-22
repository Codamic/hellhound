(ns hellhound.components.webserver-tests
  (:require [hellhound.components.webserver :as sut]
            [hellhound.routes.core          :refer [make-handler GET]]
            [com.stuartsierra.component     :as component]
            [clj-http.client                :as client]
            [clojure.test                   :refer :all]))

(def routes { "/" (fn [req] {:status 200
                              :headers {}
                              :body "Hello World" })})

(deftest webserver
  (let [subject sut/webserver
        result  (subject {} [])]
    (is (:webserver result))))

(deftest webserver-component
  (let [sys (component/start (sut/webserver routes []))]
    (try
      (println (client/get "http://localhost:4000/"))
      (finally (component/stop sys)))))
