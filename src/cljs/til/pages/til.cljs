(ns til.pages.til
  (:require [re-frame.core :as rf]
            [til.util :as u]
            [til.components :as c]
            [cljsjs.marked]))

(defn page []
  (let [id (rf/subscribe [:get-route-id])
        til (rf/subscribe [:get-til-by-id @id])]
    [:div.row
     [:div.col.l12
      [:h1 (:title @til)]
      [:div
       [:h4 (u/format-date (:date @til))]
       (for [tag (:tags @til)]
         ^{:key tag} [:a {:href (str "#/tag/" tag)} " #" tag])
       [:div {:dangerouslySetInnerHTML {:__html (js/marked (:body @til))}}]]]]))
