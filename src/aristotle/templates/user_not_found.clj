(ns aristotle.templates.user-not-found
  (:require [aristotle.config-loader :as config])
  (:use hiccup.core))



(defn render [daemon-name]
  (html
   [:html
    [:head 
     "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css' integrity='sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M' crossorigin='anonymous'>"]
    [:body 
     [:div {:class "container"}
      [:div {:class "row"}
       [:h1 (str "User: " daemon-name " is not in our system")]]
      [:div {:id "users-in-the-system"}
       [:div {:class "row"}
        [:h2 "Here are some users who are in the system:"]]
       [:div {:class "row"}
        [:ul
         (map
          (fn [n] [:li [:a {:href (str "/" n)}
                        (:daemon-name (config/get-by-name n))]])
                                        ;#(str %)
          (config/get-all-config-names))]]]]]]))
