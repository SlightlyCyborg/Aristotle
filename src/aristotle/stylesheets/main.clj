(ns aristotle.stylesheets.main
  (:require [garden.core :refer [css ]]
            [garden.stylesheet :refer [at-media]]))



(def header [[:#daemon-img {:width "100%" :margin "0"}]
             [:#header-img-atag {:margin "auto" :width "30%" :max-width "190px"}]
             [:#head-container {:text-align :center :width "100%" :margin "auto" :margin-top "50px" :margin-bottom "30px"}]
             [:.search-form {:width "70%" :margin "auto" :max-width "390px"}]
             [:#search-title {:margin "auto"}]
             [:#search {:height "35px"}]
             [:#results-div :text-align :center]])


(def search-results
  [[:.nav-btn {:margin-left "3px"}]
   [:#results-div
    [:a {:color "#0074D9"}]
    [:em {:font-weight "bold"}]
    [:.search-result-thumbnail {:float "right"}]
    [:img {:width "100%" :display "block" :margin "auto"}]
    ;;[:ul {:list-style-type "none"}]
    [:li {:display "block"}]
    [:.search-result-row
     {:margin-bottom "40px" }]
    [:.search-result-thumbnail {:margin-bottom "15px"}]
    [:a.disabled
     {:color "gray"}]]
   [:.video-wrapper {:display "none"}]])


(defn render []
  (apply css
         (apply concat
                [header
                 search-results])))

