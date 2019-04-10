(ns aristotle.templates.home
  (:require [garden.core :refer [css]]
            [aristotle.stylesheets.main :as style]
            [aristotle.config-loader :as config])
  (:use hiccup.core))


(def init-js
  "
  var to_execute = [];
  document.addEventListener('DOMContentLoaded', function() {

  document.getElementById('search').focus();
  document.getElementById('search').select();
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
  (let [{back-button-url :back-button-url
         back-button-text :back-button-text
         daemon-display-name :daemon-name
         search-bar-text :search-bar-text
         home-url :home-url}
        (config/get-by-name daemon-name)]
    (print (config/get-by-name daemon-name))
    (print search-bar-text)
   (html
    [:html
     [:title (str "Search " daemon-display-name)]
     [:head
      "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css' integrity='sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M' crossorigin='anonymous'>"
      [:link {:rel :stylesheet :href "css/menu.css"}]
      [:link {:rel :icon :href  (str "imgs/" daemon-name
                                     "/logo")}]
      [:style (style/render)]
      [:script init-js]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1.0"}]]
     [:body
      [:a {:href back-button-url}
       [:button {:type "button" :class "btn btn-secondary"}
         back-button-text]]
      [:div {:class "container" :id "head-container"}
       [:div {:class "row"}
        [:a {:href home-url :id "header-img-atag"}
         [:img {:src (str "imgs/"
                          daemon-name
                          "/logo")
                :id "daemon-img"}]]]
       [:div {:class "row search-bar-row"}
        [:form {:method "GET" :class "search-form"}
         [:div {:class "form-group"}
          [:input {:type "text"
                   :name "terms"
                   :class "form-control"
                   :id "search"
                   :placeholder search-bar-text}]]
         [:div {:class "form-group"}
          [:button {:type "submit" :class "btn btn-lg btn-primary"} "Search"]]]]]
      [:div {:id "results-div"}
       search-component]
      [:script {:src "js/jquery.js"}]
      [:script {:src "js/search.js"}]]])))
