(ns aristotle.templates.home
  (:require [garden.core :refer [css]]
            [aristotle.stylesheets.main :as style]
            [aristotle.config-loader :as config])
  (:use hiccup.core))

(defn render [daemon-name search-component]
  
    (html
     [:html
      [:head
      "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css' integrity='sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M' crossorigin='anonymous'>"
      [:style (style/render)]]
     [:body
      [:div {:class "container" :id "search-container"}
       [:div {:class "row search-header"}
        [:div {:class "col-2"}
         [:a {:href (str "/" daemon-name)}
          [:img {:src (str "imgs/"
                           daemon-name
                           "/daemon_img.png")
                 :id "daemon-img"}]]]
        [:div {:class "search-div col"}
         [:div {:class "search-div-contents"}
          [:form {:method "GET"}
           [:div {:class "form-group"}
            [:input {:type "text"
                     :name "terms"
                     :class "form-control"
                     :id "search"
                     :placeholder (str "...ask " ((config/get-by-name daemon-name) :daemon-name) " a question or search for wisdom")}]]]]]]
        [:div {:id "results-div"}
         search-component]]]]))
