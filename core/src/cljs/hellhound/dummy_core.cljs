(ns hellhound.dummy-core
  "IMPORTANT THIS NS IS JUST FOR TESTING AND IT'S NOT PART OF THE PRODUCTION
  CODE."
  (:require [cljs.nodejs :as nodejs]
            [goog.object :as gobj]
            [goog.string :as gstring]
            [clojure.string :as string]))

(nodejs/enable-util-print!)

(def -main (fn [] nil))

(set! *main-cli-fn* -main)
