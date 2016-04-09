(ns til.handlers
  (:require [ring.util.response :refer [response redirect content-type]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [til.pages :as pages]
            [til.db :refer [db]]
            [til.db.users :as users]))

(defn restricted-access
  ([req public private]
   (if-not (authenticated? req)
     public
     private))
  ([req private]
   (if-not (authenticated? req)
     (throw-unauthorized)
     private)))

(defn login [request]
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])
        session (:session request)
        user (users/get-by-name db {:username (str/lower-case username)})
        found-password (:password user)]
    (if (and found-password (hashers/check password found-password))
        (let [next-url (get-in request [:query-params :next] "/")
              pre-session (assoc session :identity (:username user))
              updated-session (assoc pre-session :uid (:id user))]
          (-> (redirect next-url)
              (assoc :session updated-session)))
      (do
        (println "wtf?")
        pages/login))))

(defn logout [request]
  (-> (redirect "/")
      (assoc :session {})))

(defn register [request]
  (let [username (get-in request [:form-params "username"])
        email    (get-in request [:form-params "email"])
        password (get-in request [:form-params "password"])
        session  (:session request)]
    (if (and username email password)
      (if-let [user (users/create db {:username username
                                      :email    email
                                      :password password})]
        (let [updated-session (-> session
                                  (assoc :identity (:username user))
                                  (assoc :uid (:id user)))]
          (-> (redirect "/")
              (assoc :session updated-session)))
        (do
          (println "wat?")
          pages/register))
      pages/register)))

(defn unauthorized [request metadata]
  (cond
    (authenticated? request)
    (-> (response "error")
        (assoc :status 403))
    :else
    (let [current-url (:uri request)]
      (redirect (str "/login?next=" current-url)))))

(defn index [request]
  (restricted-access request
                     (pages/index-non-logged-in)
                     (pages/index-logged-in (:identity (:session request)))))
