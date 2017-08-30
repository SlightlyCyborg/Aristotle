(ns elliot.templates.search
  (:require [garden.core :refer [css]]
            [clojure.pprint :as p]
            [ring.util.request :refer [request-url]])
  (:use hiccup.core))

(defn parse-search-time [search-time]
  (reduce
   str
   (map
    #(str %2 %1)
    ["h" "m" "s"]
    (clojure.string/split search-time #":"))))

(defn query-terms->url-str [query-terms]
  (str
   "?"
   (reduce
    #(str %1 "&" %2)
    (map
     (fn [[name val]] (str name "=" (clojure.string/replace val #" " "+")))
     query-terms))))

(defn render [query-result-struct page req]
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
                          (str "\"" highlight \")])
                       (:blocks doc))))]]]])
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
                       [:a {:href (str "/" (query-terms->url-str
                                            (assoc (req :params)
                                                   "page"
                                                   (str 1))))}
                        "First Page"]])]

                [:div {:class "col"}
                 (if (and (not (nil? (query-result-struct :num-found)))
                          (> page 1))
                   [:h3 {:id "second-page-headline"}
                    [:a {:href (str "/" (query-terms->url-str
                                         (assoc (req :params)
                                                "page"
                                                (str (- page 1)))))}
                     "Prev Page/"]])

                 ;;Next Page
                 (if (and (not (nil? (query-result-struct :num-found)))
                          (< (* page 10) (query-result-struct :num-found)))
                   [:h3 {:id "third-page-headline"}
                    [:a {:href (str "/" (query-terms->url-str
                                         (assoc (req :params)
                                                "page"
                                                (str (+ 1 page)))) )}
                     " Next Page"]])]]]]])))
