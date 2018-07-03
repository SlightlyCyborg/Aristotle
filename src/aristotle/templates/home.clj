(ns aristotle.templates.home
  (:require [garden.core :refer [css]]
            [aristotle.stylesheets.main :as style]
            [aristotle.config-loader :as config])
  (:use hiccup.core))


(def init-js
  "
  var to_execute = [];
  document.addEventListener('DOMContentLoaded', function() {
    var player_controls = document.getElementsByClassName('player-control');
  console.log('len' + player_controls.length);
    for(var i = 0; i < player_controls.length; i++) {
        var player_control = player_controls[i];
        player_control.onclick = function() {
            to_execute.push(this);
              return false;
        };
    }
  });
  ")

(defn render [daemon-name search-component]
    (html
     [:html
      [:title "Search Jordan Peterson"]
      [:head
      "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css' integrity='sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M' crossorigin='anonymous'>"
       [:link {:rel :stylesheet :href "css/menu.css"}]
       [:style (style/render)]
       [:script init-js]
       [:meta {:name "viewport"
               :content "width=device-width, initial-scale=1.0"}]]
     [:body
      [:a {:href "https://jordanbpeterson.com/"}
       [:img {:src "imgs/home.png" :class "home-btn"}]]
      [:div {:class "container" :id "head-container"}
       [:div {:class "row"}
        [:a {:href "/" :id "header-img-atag"}
          [:img {:src (str "imgs/"
                           daemon-name
                           "/logo.jpg")
                 :id "daemon-img"}]]]
        [:div {:class "row search-bar-row"}
          [:form {:method "GET" :class "search-form"}
           [:div {:class "form-group"}
            [:input {:type "text"
                     :name "terms"
                     :class "form-control"
                     :id "search"
                     :placeholder "search terms"}]]
           [:div {:class "form-group"}
            [:button {:type "submit" :class "btn btn-lg btn-default"} "Search"]]]]]
        [:div {:id "results-div"}
         search-component]
      [:script {:src "js/jquery.js"}]
      [:script {:src "js/search.js"}]]]))
