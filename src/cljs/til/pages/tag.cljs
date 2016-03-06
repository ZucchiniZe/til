(ns til.pages.tag
  (:require [til.db :as db]
            [til.components :as c]
            [posh.core :as p]
            [reagent.ratom :refer-macros [reaction]]
            [reagent.session :as session]))

(defn page []
  (let [tag (session/get :tag)
        pre-posts (p/q db/conn '[:find (pull ?e [:title :body :tags :date :db/id])
                                 :in $ ?tag
                                 :where
                                 [?e :tags ?tag]
                                 [?e :title ?title]
                                 [?e :body ?body]]
                       tag)
        posts (reaction (map #(% 0) @pre-posts))]
    [:div.row
     [:div.col.l12
      [:h3 "Viewing TILs tagged: #" tag]]
     (for [post @posts]
       ^{:key (:db/id post)} [c/card (:title post) (:body post) (:tags post) (:date post) (:db/id post)])]))

