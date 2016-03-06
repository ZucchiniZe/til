(ns til.components
  (:require [til.sente :as s]
            [til.util :as u]
            cljsjs.marked))

;; -------------------------
;; Reusable reagent components

(defn fab-button [til]
  [:div.fixed-action-btn {:style {:bottom 25 :right 25}}
   [:a.btn-floating.btn-large.red.waves-effect.waves-light
    (if-not til
      {:href "#/new"}
      {:onClick #(do
                   (s/chsk-send! [:til/add til])
                   (u/navigate! "/tidbits"))})
    [:i.large.material-icons (if-not til
                               "mode_edit"
                               "send")]]])

(defn card [title content tags date id]
  [:div.col.s12.m6
   [:div.card
    [:a (if id {:href (str "#/bit/" (u/id->hash id))})
     [:div.card-content.black-text
      [:span.card-title.orange-text (if date
                                      [:span title " " [:small.grey-text.text-lighten-1 (u/format-date date)]]
                                      [:span title])]
      [:div {:dangerouslySetInnerHTML {:__html (js/marked content)}}]]]
    (if-not (empty? tags)
      [:div.card-action
       (for [tag tags]
         ^{:key tag} [:a {:href (str "#/tag/" tag)} "#" tag])])]])
