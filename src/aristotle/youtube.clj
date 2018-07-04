(ns aristotle.youtube
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [aristotle.config-loader :as config]))

(def youtube-creds (config/server :youtube-credentials))

(defn video-data [ids]
  (->> (client/get "https://www.googleapis.com/youtube/v3/videos"
                   {:query-params {:key (youtube-creds :youtube-key)
                                   :part "snippet,statistics"
                                   :id ids}})))
