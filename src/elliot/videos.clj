(ns elliot.videos
  (:require [elliot.video :as video]))

(def subtitle-folder "resources/subs/elliot_hulse/")

(defn get-available-subtitle-files []
  (-> (clojure.java.io/file subtitle-folder)
      file-seq
      ((partial filter #(.isFile %)))))

(defn load-all-subtitles []
  (-> (get-available-subtitle-files)
      ((partial map #(video/make %)))))


(defn collect-all-words [subs]
  (reduce
   #(apply conj %1
           (reduce
            (fn [col block] (apply conj col (:words block)))
            []
            %2))
   []
   subs))




