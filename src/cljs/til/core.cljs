(ns til.core
  (:require [secretary.core :as secretary]
            [posh.core :as p]
            [reagent.core :as r]
            [reagent.session :as session]
            [til.db :as db]
            [til.sente :as s]
            [til.components :as c]
            cljsjs.jquery)
  (:import goog.History))

;; -------------------------
;; Initial setup

(enable-console-print!)

(p/posh! db/conn)

;; -------------------------
;; Root component

(defn root []
  (let [location (subs js/window.location.hash 1)]
    [:div
     [:div.navbar-fixed
      [:nav
       [:div.nav-wrapper.blue.darken-3
        [:a.brand-logo.center {:href "#/"} "TIL"]]]]
     [:div.container
      [(session/get :current-page)]]
     (if-not (= location "/new")
       [c/fab-button])]))

;; -------------------------
;; Init function

(defn ^:export init! []
  (s/start-router!)
  (secretary/dispatch! (subs js/window.location.hash 1))
  (r/render-component [root] (. js/document (getElementById "app"))))
