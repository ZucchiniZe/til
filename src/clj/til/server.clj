(ns til.server
  (:require [ring.middleware.defaults]
            [clojure.string     :as str]
            [compojure.core     :as comp :refer (defroutes GET POST)]
            [compojure.route    :as route]
            [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
            [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
            [org.httpkit.server :as http-kit]
            [ring.util.response :refer [response redirect content-type]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.session :refer [session-backend]]
            [environ.core :refer [env]]
            [til.sente :as sente]
            [til.pages :as pages]
            [til.handlers :as handlers]
            [til.db :refer [db]]
            [til.db.users :as users]
            [til.db.tils :as tils])
  (:gen-class))

(defroutes routes
  (GET  "/"       []       handlers/index)
  (GET  "/register" []     pages/register)
  (GET  "/login"  []       pages/login)
  (POST "/login"  []       handlers/login)
  (POST "/register" []     handlers/register)
  (GET  "/logout" []       handlers/logout)
  (GET  "/chsk"   ring-req (sente/ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"   ring-req (sente/ring-ajax-post                ring-req))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def auth-backend
  (session-backend {:unauthorized-handler handlers/unauthorized}))

(def handler
  (-> routes
      (wrap-authentication auth-backend)
      (wrap-session)
      (wrap-keyword-params)
      (wrap-params)))

(defn start-web-server! [& [port]]
  (let [port (Integer. (or port (env :port) 3000))]
    (http-kit/run-server handler {:port port})))

(defn start! [] (sente/start-router!) (start-web-server!))

(defn -main [& args] "For `lein run`, etc." [] (start!))
