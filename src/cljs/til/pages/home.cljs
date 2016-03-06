(ns til.pages.home
  (:require [til.db :as db]
            [posh.core :as p]
            [reagent.ratom :refer-macros [reaction]]))

(defn page []
  (let [total-tils (p/q db/conn '[:find (count ?e)
                                  :where [?e]])
        tils (reaction (ffirst @total-tils))]
    (fn []
      [:div.row
       [:h1 "There are " @tils " TILs in the database"]
       [:a {:href "#/tidbits"} [:h1 "TILs"]]])))
