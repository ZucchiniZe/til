(ns til.core
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [clojure.string :as str]
            [cljs.core.async :as async  :refer (<! >! put! chan)]
            [secretary.core :as secretary :refer-macros [defroute]]
            [posh.core :as p]
            [datascript.core :as d]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [reagent.session :as session]
            [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
            [taoensso.sente :as sente]
            cljsjs.marked
            cljsjs.moment
            cljsjs.hashids
            cljsjs.jquery)
  (:import goog.History))

;; -------------------------
;; Initial setup

(secretary/set-config! :prefix "#")

(enable-console-print!)

(def initial-data [{:title "Comment out a single form in clojure"
                    :body "You can use the `#_()` macro to comment out the following form and nothing else"
                    :date (js/Date.)
                    :tags ["clojure" "comment" "macro"]}
                   {:title "School is no fun"
                    :body "I have found out that school is no fun"
                    :date (js/Date.)
                    :tags ["school" "no" "fun"]}
                   {:title "Clojure is amazing"
                    :body "clojure is the best language ever for pretty much everything except rust"
                    :date (js/Date.)
                    :tags ["clojure" "awesome" "rust"]}])

(def conn (-> (d/empty-db {:tags {:db/cardinality :db.cardinality/many}})
              (d/db-with initial-data)
              (d/conn-from-db)))

(def hashid (js/Hashids. "til" 8))

(p/posh! conn)

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
  (d/transact! conn [{:title title
                      :body body
                      :date (js/Date.)
                      :tags tags}])
  (let [id (id->hash (ffirst (d/q '[:find ?e
                                    :in $ ?t
                                    :where
                                    [?e :title ?t]] @conn title)))]
    (js/Materialize.toast (str "<a class='white-text' href='#/bit/" id "'>Added TIL: " title "</a>") 5000 "green")))

;; -------------------------
;; Sente event handlers

(defmulti -event-msg-handler :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (timbre/debug id #_ev-msg)
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  #_(timbre/debug "Unhandled event: " event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (if (= ?data {:first-open? true})
    (timbre/debug "Channel socket successfully established!")
    (timbre/debug "Channel socket state change: " ?data)))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (timbre/debug "Handshake: " ?data)))

(defmethod -event-msg-handler :til/new
  [{:as ev-msg :keys [?data]}]
  (timbre/debug "got til event" ?data)
  (add-til ?data))


;; -------------------------
;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router!
           ch-chsk event-msg-handler)))

;; -------------------------
;; Reusable reagent components

(defn fab-button [til]
  [:div.fixed-action-btn {:style {:bottom 25 :right 25}}
   [:a.btn-floating.btn-large.red.waves-effect.waves-light
    (if-not til
      {:href "#/new"}
      {:onClick #(do
                   (chsk-send! [:til/add til])
                   (navigate! "/tidbits"))})
    [:i.large.material-icons (if-not til
                               "mode_edit"
                               "send")]]])

(defn card [title content tags date id]
  [:div.col.s12.m6
   [:div.card
    [:a (if id {:href (str "#/bit/" (id->hash id))})
     [:div.card-content.black-text
      [:span.card-title.orange-text (if date
                                      [:span title " " [:small.grey-text.text-lighten-1 (format-date date)]]
                                      [:span title])]
      [:div {:dangerouslySetInnerHTML {:__html (js/marked content)}}]]]
    (if-not (empty? tags)
      [:div.card-action
       (for [tag tags]
         ^{:key tag} [:a {:href (str "#/tag/" tag)} "#" tag])])]])

;; -------------------------
;; Views

(defn home-page []
  (let [total-tils (p/q conn '[:find (count ?e)
                               :where [?e]])
        tils (reaction (ffirst @total-tils))]
    (fn []
      [:div.row
       [:h1 "There are " @tils " TILs in the database"]
       [:a {:href "#/tidbits"} [:h1 "TILs"]]])))

(defn tils-page []
  (let [pre-tils (p/q conn '[:find (pull ?e [:title :body :date :tags :db/id])
                             :where
                             [?e :tags ?tags]])
        tils (reaction (reverse (sort-by #(:date %) (map #(% 0) @pre-tils))))]
    (fn []
      [:div.row
       (for [til @tils]
         ^{:key (:db/id til)} [card (:title til) (:body til) (:tags til) (:date til) (:db/id til)])])))

(defn tag-page []
  (let [tag (session/get :tag)
        pre-posts (p/q conn '[:find (pull ?e [:title :body :tags :date :db/id])
                              :in $ ?tag
                              :where
                              [?e :tags ?tag]
                              [?e :title ?title]
                              [?e :body ?body]]
                       tag)
        posts (reaction (map #(% 0) @pre-posts))]
    [:div.row
     [:div.col.l12
      [:h3 "Viewing TILs tagged: #" tag]]
     (for [post @posts]
       ^{:key (:db/id post)} [card (:title post) (:body post) (:tags post) (:date post) (:db/id post)])]))

(defn til-page []
  (let [id (session/get :id)
        pre-til (p/q conn '[:find (pull ?id [:title :body :tags :date])
                            :in $ ?id]
                     (js/parseInt id))
        til (reaction (ffirst @pre-til))]
    [:div.row
     [:div.col.l12
      [:h1 (:title @til)]
      [:div
       [:h4 (format-date (:date @til))]
       (for [tag (:tags @til)]
         ^{:key tag} [:a {:href (str "#/tag/" tag)} " #" tag])]
      [:div {:dangerouslySetInnerHTML {:__html (js/marked (:body @til))}}]]]))

(defn new-page []
  (let [title (r/atom "")
        content (r/atom "")
        tags (r/atom "")
        date (js/Date.)
        tags-arr (reaction (str/split @tags #" "))]
    (fn []
      [:div.row
       [:div.col.s12.center
        [:h2 "Make a new TIL"]]
       [:div.col.s12.m6
        [:input#title {:placeholder "Title"
                       :type "text"
                       :value @title
                       :onChange #(reset! title (.. % -target -value))}]
        [:input#tags {:placeholder "Tags (seperate by space)"
                      :type "text"
                      :value @tags
                      :onChange #(reset! tags (.. % -target -value))}]
        [:textarea#content.materialize-textarea {:placeholder "Content (markdown enabled)"
                                                 :value @content
                                                 :onChange #(reset! content (.. % -target -value))}]]
       (if-not (empty? (str @title @content @tags))
         [:div
          [card @title @content @tags-arr (if (not= @title "") date)]
          (if-not (or (empty? @title) (empty? @content) (empty? @tags))
            [fab-button {:title @title
                         :body @content
                         :tags @tags-arr}])]
         [:div.col.s12.m6
          [:h4 "Go ahead! Type something"]])])))

(defn not-found-page []
  (let [route (session/get :route)]
    [:div.row
     [:div.col.s12
      [:h1 "Sorry you have reached a " [:code "404"]]
      [:h4 route " does not exist :("]]]))

;; -------------------------
;; Root component

(defn root []
  (let [location (subs js/window.location.hash 1)]
    [:div
     [:div.navbar-fixed
      [:nav
       [:div.nav-wrapper.blue.darken-3
        [:a.brand-logo.center {:href "#/"} "TIL"]]]]
     [:div.container
      [(session/get :current-page)]]
     (if-not (= location "/new")
       [fab-button])]))

;; -------------------------
;; Routes

(defroute "/" []
  (session/put! :current-page #'home-page))

(defroute "/new" []
  (session/put! :current-page #'new-page))

(defroute "/tidbits" []
  (session/put! :current-page #'tils-page))

(defroute "/bit/:id" [id]
  (session/put! :id (hash->id id))
  (session/put! :current-page #'til-page))

(defroute "/tag/:tag" [tag]
  (session/put! :tag tag)
  (session/put! :current-page #'tag-page))

(defroute "*" {:as p}
  (session/put! :route (:* p))
  (session/put! :current-page #'not-found-page))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

;; -------------------------
;; Init function

(defn ^:export init! []
  (start-router!)
  (secretary/dispatch! (subs js/window.location.hash 1))
  (r/render-component [root] (. js/document (getElementById "app"))))
