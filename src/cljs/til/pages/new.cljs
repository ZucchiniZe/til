(ns til.pages.new
  (:require [clojure.string :as str]
            [til.components :as c]
            [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]))

(defn page []
  (let [title (r/atom "")
        content (r/atom "")
        tags (r/atom "")
        date (js/Date.)
        tags-arr (reaction (str/split @tags #" "))]
    (fn []
      [:div.row
       [:div.col.s12.center
        [:h2 "Make a new TIL"]]
       [:div.col.s12.m6
        [:input#title {:placeholder "Title"
                       :type "text"
                       :value @title
                       :onChange #(reset! title (.. % -target -value))}]
        [:input#tags {:placeholder "Tags (seperate by space)"
                      :type "text"
                      :value @tags
                      :onChange #(reset! tags (.. % -target -value))}]
        [:textarea#content.materialize-textarea {:placeholder "Content (markdown enabled)"
                                                 :value @content
                                                 :onChange #(reset! content (.. % -target -value))}]]
       (if-not (empty? (str @title @content @tags))
         [:div
          [c/card @title @content @tags-arr (if (not= @title "") date)]
          (if-not (or (empty? @title) (empty? @content) (empty? @tags))
            [c/fab-button {:title @title
                           :body @content
                           :tags @tags-arr}])]
         [:div.col.s12.m6
          [:h4 "Go ahead! Type something"]])])))
