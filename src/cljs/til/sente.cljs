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
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (timbre/debug "Unhandled event: " event))

(defmethod -event-msg-handler :til/new
  [{:as ev-msg :keys [?data]}]
  (rf/dispatch [:add-new-til ?data]))


;; -------------------------
;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router!
           ch-chsk event-msg-handler)))
