(ns elliot.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [cheshire.core :refer :all])
  (:use org.httpkit.server))

(def youtube-key "AIzaSyARW-Zw7E34dS9jvTWYYXI-532ST9VXfGk" )

(def batch-size 50)

(def es-u-name "elliottsaidwhat")

(defn response->clj [response]
  (parse-string (:body response)))

(def next-page-token (atom {}))

(defn save-page-token [data playlist-id]
  (swap! next-page-token
         assoc playlist-id (get data "nextPageToken" :no-more-pages))
  data)

(defn playlist-id->next-page-token [playlist-id]
  (get @next-page-token playlist-id))

(defn assoc-in-page-token [playlist-id m]
  (let [token (playlist-id->next-page-token playlist-id)]
   (if (not (nil? token))
     (assoc m :pageToken token)
     m)))

(defn playlist-id->single-page-video-ids [playlist-id]
  (-> @(http/get "https://www.googleapis.com/youtube/v3/playlistItems"
                 {:query-params (assoc-in-page-token
                                 playlist-id
                                 {:part "snippet"
                                  :playlistId playlist-id
                                  :key youtube-key
                                  :maxResults batch-size})})
      response->clj
      (save-page-token playlist-id)
      (get "items")
      ((partial map #(get-in % ["snippet" "resourceId" "videoId"])))))

(def elliot-vids (atom #{}))

(defn playlist-id->video-ids [playlist-id]
   (while (not (= :no-more-pages (get @next-page-token playlist-id))) 
     (swap! elliot-vids (partial apply conj) (playlist-id->single-page-video-ids playlist-id))
     (Thread/sleep 15)))

(defn get-channel-uploads-playlist-id [username]
  (-> @(http/get "https://www.googleapis.com/youtube/v3/channels"
              {:query-params {:part "contentDetails"
                              :forUsername username
                              :key youtube-key}})
      response->clj
      (get-in ["items" 0 "contentDetails" "relatedPlaylists" "uploads"])))

(def elliot-playlist-id (get-channel-uploads-playlist-id es-u-name))

(defn get-all-of-elliots-videos [] (playlist-id->video-ids elliot-playlist-id))



(defn video-id->caption-id [video-id]
  (-> @(http/get "https://www.googleapis.com/youtube/v3/captions"
                 {:query-params {:part "snippet"
                                 :videoId video-id
                                 :key youtube-key}})
      response->clj
      (get "items")
      ((partial filter #(= "en" (get-in % ["snippet" "language"]))))
      first))

(defn caption-id->caption-text [caption-id]
  @(http/get (str "https://www.googleapis.com/youtube/v3/captions/" caption-id)
             {:query-params {:key youtube-key}}))


(def thing (http/get
  "https://www.googleapis.com/youtube/v3/captions"
  {:query-params {:videoId "KDpwAPEuACs"
                  :part "snippet"
                  :key  youtube-key}}))


(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(def server (run-server app {:port 8080}))
