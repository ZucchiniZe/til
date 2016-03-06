(ns til.pages.til
  (:require [til.db :as db]
            [til.util :as u]
            [til.components :as c]
            [posh.core :as p]
            [reagent.session :as session]
            [reagent.ratom :refer-macros [reaction]]))

(defn page []
  (let [id (session/get :id)
        pre-til (p/q db/conn '[:find (pull ?id [:title :body :tags :date])
                               :in $ ?id]
                     (js/parseInt id))
        til (reaction (ffirst @pre-til))]
    [:div.row
     [:div.col.l12
      [:h1 (:title @til)]
      [:div
       [:h4 (u/format-date (:date @til))]
       (for [tag (:tags @til)]
         ^{:key tag} [:a {:href (str "#/tag/" tag)} " #" tag])]
      [:div {:dangerouslySetInnerHTML {:__html (js/marked (:body @til))}}]]]))
