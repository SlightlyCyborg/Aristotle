(ns elliot.templates.home
  (:require [garden.core :refer [css]]
            [elliot.stylesheets.main :as style])
  (:use hiccup.core))

(defn render [search-component]
  
    (html
     [:html
      [:head
      "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css' integrity='sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M' crossorigin='anonymous'>"

      [:style (style/render)]]
     [:body
      [:div {:class "container" :id "search-container"}
       [:div {:class "row search-header"}
        [:div {:class "col-2"}
         [:img {:src "imgs/small_elliot.png" :id "elliot-thumb"}]]
        [:div {:class "search-div col"}
         [:div {:class "search-div-contents"}
          [:h1 "Yo Elliot...."]
          [:form {:method "GET"}
           [:div {:class "form-group"}
            [:input {:type "text"
                     :name "terms"
                     :class "form-control"
                     :id "search"
                     :placeholder "...ask Elliot a question or search for wisdom"}]]]]]]
        [:div {:id "results-div"}
         search-component]]]]))
