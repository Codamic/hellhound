(ns hellhound.message.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]))


(s/def :hellhound.message/id uuid?)
(s/def :hellhound.message/resolver (s/fspec :args (s/cat :_ map?)
                                            :ret any?))
(s/def :hellhound.message/resolvers (s/coll-of :hellhound.message/resolver :type list?))

(s/def :hellhound.message/message
  (s/keys :req [:hellhound.message/id
                :hellhound.message/resolvers]))

(comment
  (println (gen/generate (s/gen :hellhound.message/message))))
