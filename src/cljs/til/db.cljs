(ns til.db
  (:require [datascript.core :as d]))

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
