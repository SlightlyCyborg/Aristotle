(ns aristotle.subtitles
  (:require [aristotle.youtube-captions-downloader :as youtube]
            [aristotle.config-loader :as config])
  (:import (subtitleDownloader Runner)))

(defn subs-dir [daemon-name]
  (str "/home/alex/Aristotle/resources/subs/" (name daemon-name)))

(defn urls-fname [daemon-name]
  (str (subs-dir daemon-name) "/urls.txt"))

(defn download-daemon-subtitles [daemon-name]
  (-> (Runner. (urls-fname daemon-name)
               (subs-dir daemon-name))
      (.run)))
