(ns til.util
  (:require [re-frame.core :as rf]
            [secretary.core :as secretary]
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

(defn navigate! [route]
  (secretary/dispatch! route)
  (set! js/window.location.hash (str "#" route)))
