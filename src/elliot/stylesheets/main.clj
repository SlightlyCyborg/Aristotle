(ns elliot.stylesheets.main
  (:require [garden.core :refer [css]]))



(def header
  [[:#search-container {:display :block
                        :margin :auto
                        :margin-top "30px"
                        :margin-bottom "15px"}]
   [:.search-header {}]
   [:.search-div {:position "relative"}]
   [:.search-div-contents {:position "absolute"
                           :bottom -17
                           :left 20 
                           :width "60%"}]
   [:#daemon-img {:height "140px"}]])


(def search-results
  [[:#results-div
    [:a {:color "#0074D9"}]
    [:em {:font-weight "bold"}]
    [:img {:height "90px"}]
    ;;[:ul {:list-style-type "none"}]
    [:li {:display "block"}]]
   [:.search-result-thumbnail
    {:width "30%"}]
   [:.search-result-title
    {:width "60%"}]
   [:.search-result-row
    {:margin-top "20px"}]
   [:.search-term
    {:font-weight "bold"}]
   [:.block-div
    {:margin-top "10px"}]
   [:.search-page-nav
    {:float "left"}
    [:h3
     {:float "left"}]]
   [:#first-page-headline
    {:margin-right "15px"}]])

(defn render []
  (apply css
         (apply concat
                [header
                 search-results])))

