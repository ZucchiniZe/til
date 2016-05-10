(ns til.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [til.sente :as sente]
            [til.views :as view]
            [til.routes :as routes]
            [til.handlers]
            [til.subs]))

(enable-console-print!)

(defn mount-root []
  (reagent/render [view/root]
                  (. js/document (getElementById "app"))))

(defn ^:export init! []
  (sente/start-router!)
  ;; (sente/chsk-send! [:state/req-sync])
  (routes/add-routes)
  (rf/dispatch-sync [:initalize-db])
  (mount-root))

(defn ^:export login! []
  (rf/dispatch-sync [:set-active-page :login])
  (mount-root))

(defn ^:export register! []
  (rf/dispatch-sync [:set-active-page :register])
  (mount-root))

(defn ^:export send-sync []
  (sente/chsk-send! [:sync/initial {}]))
