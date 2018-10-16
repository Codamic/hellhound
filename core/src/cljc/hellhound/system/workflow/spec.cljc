(ns hellhound.system.workflow.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))


(s/def :hellhound.workflow/source qualified-keyword?)
(s/def :hellhound.workflow/sink qualified-keyword?)
(s/def :hellhound.workflow/predicate
  (s/fspec :args (s/cat :_ :hellhound.message/message)
           :ret boolean?))

(s/def :hellhound.workflow/node
  (s/keys :req [:hellhound.workflow/source
                :hellhound.workflow/sink]
          :opt [:hellhound.workflow/predicate]))

(comment
  (clojure.pprint/pprint (s/exercise :hellhound.workflow/node)))
