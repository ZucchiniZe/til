(ns til.pages.login)

(defn page []
  [:div
   [:h1 "Login"]
   [:form {:method "POST"}
    [:input {:type "text"
             :name "username"}]
    [:input {:type "password"
             :name "password"}]
    [:input {:type "submit"}]]])
