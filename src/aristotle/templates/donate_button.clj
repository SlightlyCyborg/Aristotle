(ns aristotle.donate-button
  (:require [hiccup.core :as h]
            [aristotle.config-loader :as config]))


(def html
  (h/html
   [:div {:id "donate-div"}
    [:form {:action "https://www.paypal.com/cgi-bin/webscr"
            :method "post"
            :target "_top"}
     [:input {:type "hidden" :name "cmd" :value "_s-xclick"}]
     [:input {:type "hidden"
              :name "hosted_button_id"
              :value (config/all :donate-id)}]
     [:input {:type "image"
              :src "https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif"
              :border "0"
              :name "submit"
              :alt "PayPal"}]

     [:img {:alt ""
            :border "0"
            :src "https://www.paypalobjects.com/en_US/i/scr/pixel.gif"
            :width "1"
            :height "1"}]]
    [:p (str
         "50% goes to daemon.life creator<br>50% goes to "
         (config/all :daemon-name))]]))
