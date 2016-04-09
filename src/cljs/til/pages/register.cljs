(ns til.pages.register)

(defn page []
  [:div
   [:h1 "Register"]
   [:div.row
    [:form.col.s12 {:method "post"}
     [:div.row
      [:div.input-field.col.m6.s12
       [:input#username.validate {:type "text"
                                  :name "username"}]
       [:label {:for "username"} "username"]]
      [:div.input-field.col.m6.s12
       [:input#email.validate {:type "email"
                               :name "email"}]
       [:label {:for "email"} "e-mail"]]]
     [:div.row
      [:div.input-field.col.m6.s12
       [:input#password.validate {:type "password"
                                  :name "password"
                                  :pattern "^[A-Za-z\\d$@$!%*#?&]{8,}$"}]
       [:label {:for "password"} "password (min 8 charachters)"]]]
     [:button.btn.waves-effect.waves-light {:type "submit"}
      "Sign Up" [:i.material-icons.right "lock"]]]]])
