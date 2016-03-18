(ns til.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [til.sente :as sente]
            [til.views :as view]
            [til.routes :as routes]
            [til.handlers]
            [til.subs]))

(enable-console-print!)

(defn ^:export init! []
  (sente/start-router!)
  (routes/add-routes)
  (rf/dispatch-sync [:initalize-db])
  (reagent/render [view/root]
                  (. js/document (getElementById "app"))))
