(ns elliot.templates.home
  (:require [garden.core :refer [css]])
  (:use hiccup.core))

(defn render-css []
  (css
   [:#search-container {:display :block
                        :margin :auto
                        :margin-top "30px"
                        :margin-bottom "15px"
                        :width "50%"}]
   [:#elliot-thumb {:width "20%"}]
   [:#search-div {:margin :auto
                  :float "right"
                  :margin-top "75px"
                  :display :block
                  :width "75%"}]
   [:#search ]))

(defn render []
  (html
   [:head
    "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css' integrity='sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M' crossorigin='anonymous'>"

    [:style (render-css)]]
   [:body
    [:div {:class "container" :id "search-container"}
      [:img {:src "imgs/small_elliot.png" :id "elliot-thumb"}
       [:div {:id "search-div"}
        [:h1 "Yo Elliot...."]
        [:form
         [:div {:class "form-group"}
          [:input {:type "text"
                   :class "form-control"
                   :id "search"
                   :placeholder "...ask Elliot a question or search for wisdom"}]]]]]]]))
