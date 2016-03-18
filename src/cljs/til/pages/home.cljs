(ns til.pages.home
  (:require [re-frame.core :as rf]))

(defn page []
  (let [total-tils (rf/subscribe [:total-tils])]
    (fn []
      [:div.row
       [:h1 "There are " @total-tils " TILs in the database"]
       [:a {:href "#/tidbits"} [:h1 "TILs"]]])))
