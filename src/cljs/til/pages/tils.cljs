(ns til.pages.tils
  (:require [re-frame.core :as rf]
            [til.components :as c]))

(defn page []
  (let [tils (rf/subscribe [:get-all-tils-reverse-chronological])]
    (fn []
      [:div.row
       (for [til @tils]
         ^{:key (:id til)} [c/card til])])))
