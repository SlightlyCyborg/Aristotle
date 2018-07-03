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
       [:style (style/render)]
       [:meta {:name "viewport"
               :content "width=device-width"
               :initial-scale 1.0}]]
     [:body
      [:div {:class "container" :id "head-container"}
       [:div {:class "row"}
        [:a {:href "https://jordanbpeterson.com" :id "header-img-atag"}
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
         search-component]]]))
