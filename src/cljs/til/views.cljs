(ns til.views
  (:require [re-frame.core  :as rf]
            [til.components :as c]
            [til.pages.home :as home]
            [til.pages.tils :as tils]
            [til.pages.til  :as til]
            [til.pages.tag  :as tag]
            [til.pages.new  :as new]
            [til.pages.not-found :as not-found]))

(defmulti  page identity)
(defmethod page :home [] [home/page])
(defmethod page :tils [] [tils/page])
(defmethod page :til  [] [til/page])
(defmethod page :tag  [] [tag/page])
(defmethod page :new  [] [new/page])

(defn root []
  (let [location (subs js/window.location.hash 1)
        active-page (rf/subscribe [:active-page])]
    [:div
     [:div.navbar-fixed
      [:nav
       [:div.nav-wrapper.blue.darken-3
        [:a.brand-logo.center {:href "#/"} "TIL"]]]]
     [:div.container
      [page @active-page]
      (if (not= location "/new")
        [c/fab-button])]]))
