(ns til.db.tils
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "til/db/sql/tils.sql")
(hugsql/def-sqlvec-fns "til/db/sql/tils.sql")
