(ns til.pages
  (:require [hiccup.page :refer [include-js include-css html5]]))

(defn make-page [script]
  (html5
   [:head
    [:meta {:chatset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    (include-css "https://fonts.googleapis.com/icon?family=Material+Icons")
    (include-css "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.5/css/materialize.min.css")
    (include-css "css/style.css")
    [:script "!function(){var analytics=window.analytics=window.analytics||[];if(!analytics.initialize)if(analytics.invoked)window.console&&console.error&&console.error('Segment snippet included twice.');else{analytics.invoked=!0;analytics.methods=['trackSubmit','trackClick','trackLink','trackForm','pageview','identify','reset','group','track','ready','alias','page','once','off','on'];analytics.factory=function(t){return function(){var e=Array.prototype.slice.call(arguments);e.unshift(t);analytics.push(e);return analytics}};for(var t=0;t<analytics.methods.length;t++){var e=analytics.methods[t];analytics[e]=analytics.factory(e)}analytics.load=function(t){var e=document.createElement('script');e.type='text/javascript';e.async=!0;e.src=('https:'===document.location.protocol?'https://':'http://')+'cdn.segment.com/analytics.js/v1/'+t+'/analytics.min.js';var n=document.getElementsByTagName('script')[0];n.parentNode.insertBefore(e,n)};analytics.SNIPPET_VERSION='3.1.0';
  analytics.load('yaelC5Ac0noaYw07JsrO21ybssGXZFBj');
  analytics.page()
  }}();"]
    [:body
     [:div#app
      [:div.progress
       [:div.indeterminate]]]
     (include-js "js/jquery-2.1.1.min.js")
     (include-js "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.5/js/materialize.min.js")
     (include-js "js/compiled/til.js")
     [:script script]]]))

(defn index-logged-in [username]
  (make-page (str "window.username='" username "';til.core.init_BANG_();")))

(defn index-non-logged-in []
  (make-page (str "window.username=null;til.core.init_BANG_();")))

(def login
  (make-page (str "til.core.login_BANG_();")))

(def register
  (make-page (str "til.core.register_BANG_();")))
