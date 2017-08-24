(ns elliot.templates.search
  (:require [garden.core :refer [css]]
            [clojure.pprint :as p])
  (:use hiccup.core))

(defn parse-search-time [search-time]
  (reduce
   str
   (map
    #(str %2 %1)
    ["h" "m" "s"]
    (clojure.string/split search-time #":"))))

(defn render [query-result-struct]
  ;(p/pprint query-result-struct)
  (into [] (concat
            [:div {:class "container"}]
            (mapv
             (fn [doc]
               [:div {:class "row search-result-row"}
                 [:div {:class "search-result-thumbnail col-2"}
                  [:img {:src (:thumbnail_s doc)}]]
                 [:div {:class "search-result-title col"}
                  [:div {:class "container"}
                   [:div {:class "row"}
                    [:a {:href (str "https://www.youtube.com/watch?v="
                                    (:id doc))}
                     [:h3 (first (:title_t doc))]]]
                   [:div {:class "row"}
                    (into []
                     (cons
                      :div
                      (map
                       (fn [{highlight :highlight
                             start-time :start-time
                             stop-time :stop-time}]
                         [:div {:class "block-div"}
                          [:a
                           {:href (str "https://youtu.be/"
                                       (:id doc)
                                       "?t="
                                       (parse-search-time
                                        start-time))}
                           (str start-time " - " stop-time)]
                          "<br>"
                          (str "\"" highlight \")]
                         )
                       (:blocks doc))))]]]])
             (query-result-struct :docs)))))
