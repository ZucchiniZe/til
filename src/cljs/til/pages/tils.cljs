(ns til.pages.tils
  (:require [til.db :as db]
            [til.components :as c]
            [posh.core :as p]
            [reagent.ratom :refer-macros [reaction]]))

(defn page []
  (let [pre-tils (p/q db/conn '[:find (pull ?e [:title :body :date :tags :db/id])
                                :where
                                [?e :tags ?tags]])
        tils (reaction (reverse (sort-by #(:date %) (map #(% 0) @pre-tils))))]
    (fn []
      [:div.row
       (for [til @tils]
         ^{:key (:db/id til)} [c/card (:title til) (:body til) (:tags til) (:date til) (:db/id til)])])))
