(ns aristotle.youtube-captions-downloader
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [cheshire.core :refer :all]
            [clojure.pprint :refer [pprint]]
            [compojure.core :only [defroutes GET POST DELETE ANY context]]
            [aristotle.config-loader :as config])
  (:use org.httpkit.server
        ring.middleware.params))

;; Youtube Access Constants
(def youtube-key (get-in config/server [:youtube-credentials :youtube-key]))
(def batch-size 50)

;; Module state
(def next-page-token (atom {}))
(def vids (atom #{}))

(defn response->clj
  "Use cheshire to turn a http response string into a map of only the body"
  [response]
  (parse-string (:body response)))

(defn save-page-token
  "Statefully saves the next page token for the current video ID api query."
  [data playlist-id]
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


(defn playlist-id->video-ids [playlist-id]

  (reset! next-page-token {})
  (reset! vids #{})
  (while (not (= :no-more-pages (get @next-page-token playlist-id))) 
    (swap! vids (partial apply conj) (playlist-id->single-page-video-ids playlist-id))
    (Thread/sleep 15)))

(defn get-channel-uploads-playlist-id [id]
  (-> @(http/get "https://www.googleapis.com/youtube/v3/channels"
                 {:query-params {:part "contentDetails"
                                 :id id 
                                 :key youtube-key}})
      response->clj
      (get-in ["items" 0 "contentDetails" "relatedPlaylists" "uploads"])))

(defn get-user-uploads-playlist-id [username]
  (-> @(http/get "https://www.googleapis.com/youtube/v3/channels"
                 {:query-params {:part "contentDetails"
                                 :forUsername username
                                 :key youtube-key}})
      response->clj
      (get-in ["items" 0 "contentDetails" "relatedPlaylists" "uploads"])))




(defn get-all-uploads-list
  [daemon-name]
  (let [channel-data (:youtube-channel (config/get-by-name daemon-name))
        get-uploads-fn (case (channel-data :type)
                         :channel get-channel-uploads-playlist-id
                         :user get-user-uploads-playlist-id)]
    (playlist-id->video-ids
     (get-uploads-fn (channel-data :id)))))



(defn spit-id-urls [ids daemon-name]
  ;;Write out urls
  (let [url-root (str "resources/video_ids/" ((config/get-by-name daemon-name) :daemon-name) "/")]
   (clojure.java.io/make-parents
    (str url-root "urls"))
   (->> (into [] ids)
        (partition 600 600 [nil])
        (#(doall
           (map-indexed
            (fn [index batch]
              (spit 
               (str
                "resources/video_ids/"
                ((config/get-by-name daemon-name) :daemon-name)
                "/"
                "urls"
                index
                ".txt")
               (reduce
                (fn [str-val id]
                  (if (not (nil? id))
                    (str str-val "https://www.youtube.com/watch?v=" id ",\n")
                    str-val))
                ""
                batch)))
            %)
           )))))

(defn load-video-id-file []
  (read-string (slurp "video_ids")))


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



(defn get-all-subtitle-file-objs [srt-source-folders]
  (flatten
   (map
    (fn [subtitle-folder]
      (->> (clojure.java.io/file subtitle-folder)
          file-seq
          (filter #(.isFile %))
          (map (fn [file] {:caption-file file}))))
    srt-source-folders)))

(defn is-english-sub? [sub]
  (not (nil? (re-matches #".+_en\..+" sub))))

(defn get-id [item]
  (subs item 0 11))

(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)))


(defn get-version [item]
  (parse-number (subs item 12 13)))

(defn get-last-srt-version [tmp-data item]
  (let [id (get-id item)
        version (get-version item)]
    (if (< (get-in tmp-data [id :version] -1)
           version)
      (assoc tmp-data id {:version version :file item})
      tmp-data)))


(defn gen-captions-to-delete [daemon-name]
  (let [file (->>
              ((config/get-by-name daemon-name) :srt-source-folders)
              get-all-subtitle-file-objs
              (map #(.getName (:caption-file %))))]
    (->>
     file
     (filter is-english-sub?)
     (reduce get-last-srt-version {})
     (map #(:file (second %)))
     (set)
     (clojure.set/difference (set file))
     (reduce (fn [s f] (str s " ./" f)) "")
     (#(spit (str (first ((config/get-by-name daemon-name) :srt-source-folders)) "/rm-script.sh") (str "rm " %)))))) 

(defn daemon-name->video-id-file
  [daemon-keyword]
  (get-all-uploads-list daemon-keyword)
  (spit-id-urls @vids daemon-keyword))

