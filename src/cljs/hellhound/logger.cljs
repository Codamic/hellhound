(ns hellhound.logger
  "Javascript logger for hellhound client.")

(def original (atom {}))

(def levels
  {:trace 10
   :log   20
   :info  40
   :warn  60
   :error 80})

;; (defn replace-level!
;;   [desired-level level]
;;   (if (=> (level levels) (desired-level levels))
;;     (do
;;       (def (name level (get js/console (str "-" (name level))))))
;;     (def (name level) (fn [& _]))))


;; (defn set-level
;;   [level]
;;   (map #(replace-level! level %) levels))

;; TODO: Remove these functions and implement the set-level
;;       instead
(defn log
  [& rest]
  (apply js/console :log rest))

(def debug log)

(defn info
  [& rest]
  (apply js/console :info rest))

(defn warn
  [& rest]
  (apply js/console :warn rest))

(defn error
  [& rest]
  (apply js/console :error rest))
