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
                           :bottom 0
                           :width "60%"}]])


(def search-results
  [[:#results-div
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
    {:margin-top "10px"}]])

(defn render []
  (apply css
         (apply concat
                [header
                 search-results])))

