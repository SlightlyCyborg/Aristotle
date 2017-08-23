(ns elliot.templates.search
  (:require [garden.core :refer [css]]
            [clojure.pprint :as p])
  (:use hiccup.core))


(def mockup-data
  {:docs
   [{:id "OPVWU6oSPZg",
     :title_t ["Be More AGGRESSIVE!!"]
     :description_t ["Sign up Elliott's Newsletter: http://hulsestrength.com/go/youtube\n\nStrength YouTube Channel: http://www.youtube.com/strengthcamp\n\nElliott's Facebook: https://www.facebook.com/elliotthulse\n\nNonjob Revolution: http://nonjob.com\n\nStrength Blog: http://www.HulseStrength.com"]
     :uploaded_dt "2014-03-21T23:00:01Z"
     :likes_i 7796,
     :views_i 306498,
     :channel_title_s "Elliott Hulse",
     :captions_t ["cut those questions one of our buddies wants to know how to be more aggressive"]
     :highlight-html ["cut those questions one of our buddies wants to know how to be more <span class=\"search-term\">aggressive</span>"]
     :thumbnail_s  "https://i.ytimg.com/vi/5CjC68fvoSM/sddefault.jpg"
     :_version_ "1576457191610646528"}

    {:id "p-GX8M919hg"
     :title_t ["The Tender Aggressive Man (real power)"]
     :description_t ["Sign up Elliott's Newsletter: http://hulsestrength.com/go/youtube\n\nStrength YouTube Channel: http://www.youtube.com/strengthcamp\n\nElliott's Facebook: https://www.facebook.com/elliotthulse\n\nNonjob Revolution: http://nonjob.com\n\nStrength Blog: http://www.HulseStrength.com"]
     :uploaded_dt "2013-06-18T23:00:47Z"
     :likes_i 4597
     :views_i 260872
     :channel_title_s "Elliott Hulse"
     :captions_t [" so we've got a fascinating question from our buddy who calls himself the split tongue "]
     :highlight-html  ["so we've got a fascinating question from our buddy who calls himself the <span class=\"search-term\">split tongue</span>"

                       "cut those questions one of our buddies wants to know how to be more <span class=\"search-term\">aggressive</span>"]
     :thumbnail_s  "https://i.ytimg.com/vi/JJaVbJUvlys/sddefault.jpg"
     :_version_ 1576457240885329920}]})

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
