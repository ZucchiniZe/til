(ns til.pages.not-found
  (:require [reagent.session :as session]))

(defn page []
  (let [route (session/get :route)]
    [:div.row
     [:div.col.s12
      [:h1 "Sorry you have reached a " [:code "404"]]
      [:h4 route " does not exist :("]]]))
