(ns til.util
  (:require [secretary.core :as secretary]
            [datascript.core :as d]
            [til.db :as db]
            cljsjs.hashids
            cljsjs.moment))

(def hashid (js/Hashids. "til" 8))

;; -------------------------
;; Utility functions

(defn id->hash [id]
  (.encode hashid id))

(defn hash->id [hash]
  (.decode hashid hash))

(defn format-date [date]
  (. (js/moment date) (format "MMM Do YYYY")))

(defn navigate! [route]
  (secretary/dispatch! route)
  (set! js/window.location.hash (str "#" route)))

(defn add-til [{:keys [title body tags] :as til}]
  (d/transact! db/conn [{:title title
                         :body body
                         :date (js/Date.)
                         :tags tags}])
  (let [id (id->hash (ffirst (d/q '[:find ?e
                                    :in $ ?t
                                    :where
                                    [?e :title ?t]] @db/conn title)))]
    (js/Materialize.toast (str "<a class='white-text' href='#/bit/" id "'>Added TIL: " title "</a>") 5000 "green")))
