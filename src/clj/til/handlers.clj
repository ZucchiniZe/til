(ns til.handlers
  (:require [ring.util.response :refer [response redirect content-type]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [til.pages :as pages]
            [til.db :refer [db]]
            [til.db.users :as users]))

(defn login-authenticate [request]
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])
        session (:session request)
        user (users/get-by-name db {:username username})
        found-password (:password user)]
    (if (and found-password (= found-password password))
        (let [next-url (get-in request [:query-params :next] "/")
              pre-session (assoc session :identity username)
              updated-session (assoc pre-session :uid (:id user))]
          (-> (redirect next-url)
              (assoc :session updated-session)))
      (do
        (println "wtf?")
        pages/login))))

(defn logout [request]
  (-> (redirect "/")
      (assoc :session {})))

(defn unauthorized [request metadata]
  (cond
    (authenticated? request)
    (-> (response "error")
        (assoc :status 403))
    :else
    (let [current-url (:uri request)]
      (redirect (str "/login?next=" current-url)))))
