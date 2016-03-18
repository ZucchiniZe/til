(ns til.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]))

(defn get-by-id [collection id]
  (first (get (group-by :id collection) id)))

(defn get-by-tag [coll tag]
  (for [data coll
        wanted [tag]
        :when (some #{wanted} (:tags data))]
    data))

(rf/register-sub
 :active-page
 (fn [db]
   (reaction (:current-route @db))))

(rf/register-sub
 :total-tils
 (fn [db]
   (reaction (count (:tils @db)))))

(rf/register-sub
 :get-route-tag
 (fn [db]
   (reaction (-> @db :route :tag))))

(rf/register-sub
 :get-route-id
 (fn [db]
   (reaction (-> @db :route :id))))

(rf/register-sub
 :get-route-not-found
 (fn [db]
   (reaction (-> @db :route :not-found))))

(rf/register-sub
 :get-til-by-id
 (fn [db [_ id]]
   (reaction (get-by-id (:tils @db) id))))

(rf/register-sub
 :get-til-by-tag
 (fn [db [_ tag]]
   (reaction (get-by-tag (:tils @db) tag))))

(rf/register-sub
 :get-all-tils-reverse-chronological
 (fn [db]
   (->> (:tils @db)
        (sort-by #(:date %))
        (reverse)
        (reaction))))

(rf/register-sub
 :current-id
 (fn [db]
   (reaction (:current-id @db))))
