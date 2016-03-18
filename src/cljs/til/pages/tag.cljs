(ns til.pages.tag
  (:require [re-frame.core :as rf]
            [til.components :as c]))

(defn page []
  (let [tag (rf/subscribe [:get-route-tag])
        tils (rf/subscribe [:get-til-by-tag @tag])]
    [:div.row
     [:div.col.l12
      [:h3 "Viewing TILs tagged: #" @tag]]
     (for [til @tils]
       ^{:key (:id til)} [c/card til])]))
