(ns til.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.session :as session]
            [til.util :as u]
            [til.pages.home :as home]
            [til.pages.tils :as tils]
            [til.pages.tag :as tag]
            [til.pages.til :as til]
            [til.pages.new :as new]
            [til.pages.not-found :as not-found])
  (:import goog.History))

(secretary/set-config! :prefix "#")

(defroute "/" []
  (session/put! :current-page #'home/page))

(defroute "/new" []
  (session/put! :current-page #'new/page))

(defroute "/tidbits" []
  (session/put! :current-page #'tils/page))

(defroute "/bit/:id" [id]
  (session/put! :id (u/hash->id id))
  (session/put! :current-page #'til/page))

(defroute "/tag/:tag" [tag]
  (session/put! :tag tag)
  (session/put! :current-page #'tag/page))

(defroute "*" {:as p}
  (session/put! :route (:* p))
  (session/put! :current-page #'not-found/page))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))
