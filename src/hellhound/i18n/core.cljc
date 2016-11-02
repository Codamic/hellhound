(ns hellhound.i18n.core
  "I18n features of HellHound is based on [tempura](https://github.com/ptaoussanis/tempura)
  The `initialize-i18n` function should be called at the beginning of the
  application in order to set the default configuration and dictionary.

  The required dictionary should be in the following format:

  ```clojure
  (def my-dict
    {:en-GB ; Locale
     {:missing \":en-GB missing text\" ; Fallback for missing resources
      :example ; You can nest ids if you like
      {:greet \"Good day %1!\" ; Note Clojure fn-style %1 args
     }}

   :en ; A second locale
   {:missing \":en missing text\"
    :example
    {:greet \"Hello %1\"
     :farewell \"Goodbye %1\"
     :foo \"foo\"
     :bar-copy :en.example/foo ; Can alias entries
     :baz [:div \"This is a **Hiccup** form\"]}

    :example-copy :en/example ; Can alias entire subtrees

    :import-example
    {:__load-resource ; Inline edn content loaded from disk/resource
     \"resources/i18n.clj\"}}})
  ```

  And the initialization should be like:


  ```clojure
  (init my-dict :en)

  ; You can change the current language later like this:

  (set-locale! :fa)
  ```

  "
  (:require [taoensso.tempura :as tempura :refer [tr]]))


(def ^:private locale  (atom nil))
(def ^:private options (atom nil))

(defn- check-for-valid-values
  []
  (if (nil? @locale)
    (throw #?(:clj (Exception. "Current locale is nil. You forgot to set it.")
              :cljs "Current locale is nil. You forgot to set it."))
    (do (if (nil? @options)
          (throw #?(:clj  (Exception. "Did you set the default dictionary for i18n system ?")
                    :cljs "Did you set the default dictionary for i18n system ?"))))))

(defn set-locale!
  "Set the current locale to the given value."
  [lang]
  (reset! locale lang))

(defn t
  "Translate the given string according to the current locale."
  [& args]
  (check-for-valid-values)
  (tr @options [@locale] (first (vec args))))



(defn init
  "Set the default dictionary of the i18n system. This function should
  be call at the begining of your application."
  [dictionary lang]
  (set-locale! lang)
  (reset! options {:dict dictionary}))
