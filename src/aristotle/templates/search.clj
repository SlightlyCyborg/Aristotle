(ns aristotle.templates.search
  (:require [garden.core :refer [css]]
            [clojure.pprint :as p]
            [ring.util.request :refer [request-url]]
            [aristotle.templates.donate-button :as donate-button])
  (:use hiccup.core))

(defn parse-search-time [search-time]
  (reduce
   str
   (map
    #(str %2 %1)
    ["h" "m" "s"]
    (clojure.string/split search-time #":"))))

(defn parse-search-time-for-player-api [search-time]
  (reduce
   +
   (map
    #(* %1 (Integer/parseInt %2))
    [3600 60 1]
    (clojure.string/split search-time #":"))))

(defn query-terms->url-str [query-terms]
  (str
   "?"
   (reduce
    #(str %1 "&" %2)
    (map
     (fn [[name val]] (str name "=" (clojure.string/replace val #" " "+")))
     query-terms))))

(defn render [query-result-struct page req daemon-name]
  (into [] (concat
            [:div {:class "container" :id "search-container"}]
            (mapv
             (fn [doc]
               [:div {:class "row search-result-row"}
                 [:div {:class "search-result-thumbnail col-md-4"}
                  [:img {:src (:thumbnail_s doc)
                         :id (str "img-"
                                  (:id doc))
                         :class "thumbnail"
                         :data-video-id (:id doc)}]
                  [:div {:class "video-wrapper"
                         :id (str "video-wrapper-" (:id doc))
                         :data-video-id (:id doc)}
                   [:div {:class "video"
                          :id (str "video-" (:id doc))}]]]
                 [:div {:class "search-result-title col-md-8"}
                  [:div {:class "container"}
                   [:div {:class "row"}
                    [:h3 (first (:title_t doc))]]
                   [:div {:class "row"}
                    (into []
                     (cons
                      :div
                      (map
                       (fn [{highlight :highlight
                             start-time :start-time
                             stop-time :stop-time}
                            x]
                         [:div {:class "block-div"}
                          [:a
                           {:href (str "https://youtu.be/"
                                       (:id doc)
                                       "?t="
                                       (parse-search-time
                                        start-time))
                            :data-video-id (:id doc)
                            :data-x x
                            :class (str
                                    "player-control"
                                    " "
                                    (str "player-control-for-"
                                         (:id doc)))
                            :data-time (parse-search-time-for-player-api
                                        start-time)
                             }
                           (str start-time " - " stop-time)]
                          [:div {:id (str "loader-" (:id doc) "-" x)
                                 :style "display:none;"} "foo"]
                          "<br>"
                          (str "\"" highlight \")])
                       (:blocks doc)
                       (range))))]]]])
             (query-result-struct :docs))
            [
             [:div {:class "container"}
              [:div {:class "row"}
               [:div {:class "col-2"}]
               [:div {:class "search-page-nav"}

                ;;Prev Page
                [:div
                 {:class "col"}

                 (if (and (not (nil? (query-result-struct :num-found)))
                             (> page 1))
                      [:h3 {:id "first-page-headline"}
                       [:a {:href (str "/"
                                       (query-terms->url-str
                                        (-> (req :params)
                                            (dissoc :daemon-name)
                                            (assoc 
                                             "page"
                                             (str 1)))))}
                        "First Page"]])]

                [:div {:class "col"}
                 (if (and (not (nil? (query-result-struct :num-found)))
                          (> page 1))
                   [:h3 {:id "second-page-headline"}
                    [:a {:href (str "/"
                                    (query-terms->url-str
                                     (-> (req :params)
                                         (dissoc :daemon-name)
                                         (assoc 
                                          "page"
                                          (str (- page 1))))))}
                     "Prev Page/"]])

                 ;;Next Page
                 (if (and (not (nil? (query-result-struct :num-found)))
                          (< (* page 10) (query-result-struct :num-found)))
                   [:h3 {:id "third-page-headline"}
                    [:a {:href (str "/"
                                    (query-terms->url-str
                                     (-> (req :params)
                                         (dissoc :daemon-name)
                                         (assoc 
                                          "page"
                                          (str (+ 1 page))))))}
                     " Next Page"]])]]]]])))
