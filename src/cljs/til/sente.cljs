(ns til.sente
  (:require [taoensso.sente :as sente]
            [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
            [re-frame.core :as rf]
            [til.util :as u]))

;; -------------------------
;; Sente setup

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk"
                                         {:packer :edn
                                          :type :auto
                                          :wrap-recv-evs? false})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))


;; -------------------------
;; Sente event handlers

(defmulti -event-msg-handler :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  #_(timbre/debug id #_ev-msg)
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  )

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (when (:first-open? ?data)
    (println "requesting data")
    (rf/dispatch [:initial-sync])))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  )

(defmethod -event-msg-handler :til/new
  [{:as ev-msg :keys [?data]}]
  ;; (timbre/debug "got til event" ?data)
  (rf/dispatch [:add-new-til ?data]))

(defmethod -event-msg-handler :sync/data
  [{:as ev-msg :keys [?data]}]
  (rf/dispatch [:sync-tils (:tils ?data)]))

;; (defmethod -event-msg-handler :state/recv-sync
;;   [{:as ev-msg :keys [?data]}]
;;   ;; TODO: write the client side sync code here
;;   (println ?data))


;; -------------------------
;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router!
           ch-chsk event-msg-handler)))

(def cb-success? sente/cb-success?)
