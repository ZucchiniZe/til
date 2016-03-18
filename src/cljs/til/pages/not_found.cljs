(ns til.pages.not-found
  (:require [re-frame.core :as rf]))

(defn page []
  (let [route (rf/subscribe [:get-route-not-found])]
    [:div.row
     [:div.col.s12
      [:h1 "Sorry you have reached a " [:code "404"]]
      [:h4 @route " does not exist :("]]]))
