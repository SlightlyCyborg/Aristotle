(ns aristotle.videos
  (:require [aristotle.video :as video]))

(def subtitle-folder "resources/subs/aristotle_hulse/")

(defn get-available-subtitle-files []
  (-> (clojure.java.io/file subtitle-folder)
      file-seq
      ((partial filter #(.isFile %)))))

(defn load-all-videos []
  (-> (get-available-subtitle-files)
      ((partial map #(video/make %)))))





