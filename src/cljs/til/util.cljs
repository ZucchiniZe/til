(ns til.util
  (:require [re-frame.core :as rf]
            [cljsjs.hashids]
            [cljsjs.moment]))

(def hashid (js/Hashids. "til" 8))

;; -------------------------
;; Utility functions

(defn id->hash [id]
  (.encode hashid id))

(defn hash->id [hash]
  (.decode hashid hash))

(defn format-date [date]
  (. (js/moment date) (format "MMM Do YYYY")))

(defn add-til [{:keys [title body tags] :as til}]
  (rf/dispatch [:add-new-til til])
  (let [id (id->hash (rf/subscribe [:current-id]))]
    (js/Materialize.toast (str "<a class='white-text' href='#/bit/" id "'>Added TIL: " title "</a>") 5000 "green")))
