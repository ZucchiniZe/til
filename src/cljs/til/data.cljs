(ns til.data)

(def initial-db {:current-route :home
                 :userid nil
                 :current-id 1
                 :route {:id nil
                         :tag nil
                         :not-found nil}
                 :tils [{:id 0
                         :title "Comment out a single form in clojure"
                         :body "You can use the `#_()` macro to comment out the following form and nothing else"
                         :date (js/Date.)
                         :tags ["clojure" "comment" "macro"]}
                        {:id 1
                         :title "Clojure is amazing"
                         :body "clojure is the best language ever for pretty much everything except rust"
                         :date (js/Date.)
                         :tags ["clojure" "awesome"]}]})
