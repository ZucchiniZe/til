(ns til.pages.login)

(defn page []
  [:div
   [:h1 "Login"]
   [:div.row
    [:form.col.s12 {:method "post"}
     [:div.row
      [:div.input-field.col.m6.s12
       [:input#username.validate {:type "text"
                                  :name "username"}]
       [:label {:for "username"} "Username"]]
      [:div.input-field.col.m6.s12
       [:input#password.validate {:type "password"
                                  :name "password"}]
       [:label {:for "password"} "Password"]]]
     [:button.btn.waves-effect.waves-light {:type "submit"}
      "Log In" [:i.material-icons.right "send"]]]]])
