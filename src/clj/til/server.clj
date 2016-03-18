(ns til.server
  (:require [clojure.string     :as str]
            [ring.middleware.defaults]
            [compojure.core     :as comp :refer (defroutes GET POST)]
            [compojure.route    :as route]
            [hiccup.page        :refer [include-js include-css html5]]
            [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
            [taoensso.encore    :as encore :refer ()]
            [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
            [taoensso.sente     :as sente]

            [org.httpkit.server :as http-kit]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)])
  (:gen-class))

(defn start-selected-web-server! [ring-handler port]
  (infof "Starting http-kit...")
  (let [stop-fn (http-kit/run-server ring-handler {:port port})]
    {:server  nil ; http-kit doesn't expose this
     :port    (:local-port (meta stop-fn))
     :stop-fn (fn [] (stop-fn :timeout 100))}))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket-server! sente-web-server-adapter)]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn)
  (def connected-uids                connected-uids))

;;;; Ring handlers

(def index-page
  (html5
   [:head
    [:meta {:chatset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    (include-css "https://fonts.googleapis.com/icon?family=Material+Icons")
    (include-css "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.5/css/materialize.min.css")
    (include-css "css/style.css")]
   [:body
    [:div#app
     [:div.progress
      [:div.indeterminate]]]
    (include-js "js/jquery-2.1.1.min.js")
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.5/js/materialize.min.js")
    (include-js "js/compiled/til.js")
    [:script "til.core.init_BANG_();"]]))

;; (defn login-handler
;;   [ring-req]
;;   (let [{:keys [session params]} ring-req
;;         {:keys [user-id]} params]
;;     (debugf "Login request: %s" params)
;;     {:status 200 :session (assoc session :uid user-id)}))

(defroutes ring-routes
  (GET  "/"      []       index-page)
  (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
  ;; (POST "/login" ring-req (login-handler                 ring-req))
  (route/resources "/") ; Static files, notably public/main.js (our cljs target)
  (route/not-found "<h1>Page not found</h1>"))

(def main-ring-handler
  "**NB**: Sente requires the Ring `wrap-params` + `wrap-keyword-params`
  middleware to work. These are included with
  `ring.middleware.defaults/wrap-defaults` - but you'll need to ensure
  that they're included yourself if you're not using `wrap-defaults`."
  (ring.middleware.defaults/wrap-defaults
   ring-routes ring.middleware.defaults/site-defaults))

;;;; Sente event functions

(defn add-new-til [til]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid [:til/new til])))

;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

(defmethod -event-msg-handler :chsk/ws-ping
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  ;; (debugf "got a ping")
  (when ?reply-fn
    (?reply-fn {:echoed-ping event})))

(defmethod -event-msg-handler :til/add
  [{:as ev-msg :keys [?data]}]
  (add-new-til ?data))

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-server-chsk-router!
      ch-chsk event-msg-handler)))

;;;; Some server>user async push examples

(defn start-example-broadcaster!
  "As an example of server>user async pushes, setup a loop to broadcast an
  event to all connected users every 10 seconds"
  []
  (let [broadcast!
        (fn [i]
          (debugf "Broadcasting server>user: %s" @connected-uids)
          (doseq [uid (:any @connected-uids)]
            (chsk-send! uid
              [:some/broadcast
               {:what-is-this "An async broadcast pushed from server"
                :how-often "Every 10 seconds"
                :to-whom uid
                :i i}])))]

    (go-loop [i 0]
      (<! (async/timeout 10000))
      (broadcast! i)
      (recur (inc i)))))

(defn test-fast-server>user-pushes
  "Quickly pushes 100 events to all connected users. Note that this'll be
  fast+reliable even over Ajax!"
  []
  (doseq [uid (:any @connected-uids)]
    (doseq [i (range 100)]
      (chsk-send! uid [:fast-push/is-fast (str "hello " i "!!")]))))

;;;; Init stuff

(defonce    web-server_ (atom nil)) ; {:server _ :port _ :stop-fn (fn [])}
(defn  stop-web-server! [] (when-let [m @web-server_] ((:stop-fn m))))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [{:keys [stop-fn port] :as server-map}
        (start-selected-web-server! (var main-ring-handler)
          (or port 0) ; 0 => auto (any available) port
          )
        uri (format "http://localhost:%s/" port)]
    (infof "Web server is running at `%s`" uri)
    (reset! web-server_ server-map)))

(defn stop!  []  (stop-router!)  (stop-web-server!))
(defn start! [] (start-router!) (start-web-server!) (start-example-broadcaster!))
;; (defonce _start-once (start!))

(defn -main "For `lein run`, etc." [] (start!))

(comment
  (start!)
  (test-fast-server>user-pushes))
