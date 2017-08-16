(ns elliot.videos
  (:require [elliot.video :as video]))

(def subtitle-folder "resources/subs/elliot_hulse/")

(defn get-available-subtitle-files []
  (-> (clojure.java.io/file subtitle-folder)
      file-seq
      ((partial filter #(.isFile %)))))

(defn load-all-videos []
  (-> (get-available-subtitle-files)
      ((partial map #(video/make %)))))





