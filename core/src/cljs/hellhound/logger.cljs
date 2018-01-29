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
;; (defn log
;;   [& rest]
;;   (let [log (.bind (.-log js/console) js/console)]
;;     (mapv log rest)))


(defn log
  [& rest]
  (let [log (.bind (.-info js/window.console) js/window.console)]
    (mapv log rest)))

(def debug log)

(defn info
  [& rest]
  (let [log (.bind (.-info js/console) js/console)]
    (mapv log rest)))

(defn warn
  [& rest]
  (let [log (.bind (.-warn js/console) js/console)]
    (mapv log rest)))


(defn error
  [& rest]
  (let [log (.bind (.-error js/console) js/console)]
    (mapv log rest)))
