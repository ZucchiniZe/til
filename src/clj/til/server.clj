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
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.session :refer [session-backend]]
            [til.sente :as sente]
            [til.pages :as pages]
            [til.handlers :as handlers]
            [til.db :refer [db]]
            [til.db.users :as users]
            [til.db.tils :as tils])
  (:gen-class))

(defn restricted-access
  ([req public private]
   (if-not (authenticated? req)
     public
     private))
  ([req private]
   (if-not (authenticated? req)
     (throw-unauthorized)
     private)))

(defroutes routes
  (GET  "/"      []       pages/index)
  (GET  "/login" []       pages/login)
  (POST "/login" []       handlers/login-authenticate)
  (GET  "/logout" []      handlers/logout)
  (GET  "/restricted" [] (restricted-access (response "hi")))
  (GET  "/chsk"  ring-req (sente/ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (sente/ring-ajax-post                ring-req))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def auth-backend
  (session-backend {:unauthorized-handler handlers/unauthorized}))

(defn start-web-server! [& [port]]
  (-> routes
      (wrap-authentication auth-backend)
      (wrap-params)
      (wrap-keyword-params)
      (wrap-session)
      (http-kit/run-server {:port 3000})))

(defn start! [& args] (sente/start-router!) (start-web-server! args))

(defn -main [& args] "For `lein run`, etc." []
  (start! args))
