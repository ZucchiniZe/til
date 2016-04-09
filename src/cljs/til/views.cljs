(ns til.views
  (:require [re-frame.core  :as rf]
            [til.components :as c]
            [til.pages.home :as home]
            [til.pages.tils :as tils]
            [til.pages.til  :as til]
            [til.pages.tag  :as tag]
            [til.pages.new  :as new]
            [til.pages.login :as login]
            [til.pages.register :as register]
            [til.pages.not-found :as not-found]))

(defmulti  page identity)
(defmethod page :home [] [home/page])
(defmethod page :tils [] [tils/page])
(defmethod page :til  [] [til/page])
(defmethod page :tag  [] [tag/page])
(defmethod page :new  [] [new/page])
(defmethod page :login [] [login/page])
(defmethod page :register [] [register/page])
(defmethod page :not-found [] [not-found/page])

(defn root []
  (let [location (subs js/window.location.hash 1)
        active-page (rf/subscribe [:active-page])]
    [:div
     [:div.navbar-fixed
      [:nav
       [:div.nav-wrapper.blue.darken-3
        [:a.brand-logo.center {:href "#/"} "TIL"]
        (let [username js/window.username
              authenticated? (nil? username)]
          (if-not authenticated?
            [:ul.right
             [:li [:a {:href "#/profile"} [:i.left.material-icons "person"] username]]
             [:li [:a {:href "/logout"} [:i.material-icons "exit_to_app"]]]]
            [:ul.right
             [:li [:a {:href "/login"} "login"]]
             [:li [:a {:href "/register"} "register"]]]))]]]
     [:div.container
      [page @active-page]
      (if (and (not= location "/new") (not (nil? js/window.username)))
        [c/fab-button])]]))
