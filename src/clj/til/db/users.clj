(ns til.db.users
  (:require [hugsql.core :as hugsql]
            [buddy.hashers :as hashers]))

(hugsql/def-db-fns "til/db/sql/users.sql")
(hugsql/def-sqlvec-fns "til/db/sql/users.sql")

(defn create [db props]
  (create-sql db (update props :password hashers/derive)))
