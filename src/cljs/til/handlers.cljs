(ns til.handlers
  (:require [re-frame.core :as rf]
            [til.db :as db]
            [til.util :as u]))

(rf/register-handler
 :initalize-db
 rf/trim-v
 (fn []
   db/initial-db))

(rf/register-handler
 :set-active-page
 rf/trim-v
 (fn [db [page]]
   (assoc db :current-route page)))

(rf/register-handler
 :set-route-id
 rf/trim-v
 (fn [db [id]]
   (assoc-in db [:route :id] (first id))))

(rf/register-handler
 :set-route-tag
 rf/trim-v
 (fn [db [tag]]
   (assoc-in db [:route :tag] tag)))

(rf/register-handler
 :set-route-not-found
 rf/trim-v
 (fn [db [path]]
   (assoc-in db [:route :not-found] path)))

(rf/register-handler
 :add-new-til
 rf/trim-v
 (fn [db [new-til]]
   (let [id (:current-id db)
         tils (:tils db)
         with-id (assoc new-til :id (inc id))
         with-date (assoc with-id :date (js/Date.))]
     (-> db
         (assoc :tils (conj tils with-date))
         (assoc :current-id (inc id))))))
