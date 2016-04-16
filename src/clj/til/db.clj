(ns til.db
  (:require [til.db.users :as users]
            [til.db.tils  :as tils]
            [environ.core :refer [env]]
            [heroku-database-url-to-jdbc.core :as h]))

(def database-url (or (env :database-url) "postgres://postgres:mysecretpassword@localhost:5432/tils"))

(def db (h/korma-connection-map database-url))

(users/create-users-table db)
(tils/create-tils-table db)
