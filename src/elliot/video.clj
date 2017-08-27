(ns elliot.video
  (:use clojure.test
        clojure.pprint)
  (:require [clojure.spec.alpha :as s]
            [elliot.nlp-utils :as nlp-utils]
            [elliot.config-loader :as config]
            [elliot.youtube :as youtube]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clj-solr.core :as solr])
  (:import [opennlp.tools.tokenize WhitespaceTokenizer]
           [opennlp.tools.postag POSTaggerME POSModel]))


(s/def ::words (s/coll-of string?))
(s/def ::block-id int?)

(s/def ::block (s/keys :req [::block-id ::words]))
(s/def ::blocks (s/coll-of ::block))

(defn parse-int
  "Takes a string and parses out an integer using the regex \\d+."
  [s]
  (try (Integer. (re-find  #"\d+" s ))
       (catch Exception e 0)))

(defn parse-block-time
  "Converts the time block in the .srt file and turns it into a data structure"
  [block-time-text]
  (->> block-time-text
        (#(clojure.string/split % #" --> "))
       (map #(clojure.string/split % #"[:,]"))
       (map (fn [t] (map #(parse-int %) t)))
       (map #(interleave [:hour :minute :second :millis] %))
       (map #(apply hash-map %))
       (interleave [:start :stop])
       (apply hash-map)))

(defn time-struct->string
  "Takes the time-struct and turns it into a colon seperated string"
  [{h :hour m :minute s :second mi :millis}]
  (str h ":" m ":" s ":" mi))

(deftest test-parse-block-time
  (let [test-data "00:05:01,670 --> 00:05:06,970"]
    (is (=
         (parse-block-time test-data)
         {:start {:hour 0 :second 1 :millis 670 :minute 5}
          :stop  {:hour 0 :second 6 :millis 970 :minute 5}}))))

(defn load-caption-blocks
  "Takes an srt file and assocs in ::block-id ::words ::time"
  [f]
  (-> (slurp f)
      (clojure.string/replace #"<[^>]+>" "")
      (clojure.string/split #"\r\n\r\n")
      ((partial map #(clojure.string/split % #"\r\n")))
      ((partial map #(do {::block-id (parse-int (first %))
                          ::words (nth % 2)
                          ::time (parse-block-time (second %))})))))

(defn de-blockify
  "Makes a single list from a key in blocks"
  [video key]
  (reduce
   #(concat %1 (key %2))
   [] 
   video))


(defn de-blockify-words [video]
  (reduce
   #(str %1 " " (::words %2))
   ""
   video))




(defn blockify-list
   "Divides a list up into blocks of the same length as the control data structure. If total item count of list & control data structure do not equal, then an error is thrown"
  [l video control-key]
  (if (not (= (count (de-blockify video ::words)) (count l)))
    (throw (Exception. "The list is not same size as control words")))

  (let [l (vec l) ;;Need data to be in vec for subvec

        list-of-sizes ;;Build up the list-of-sizes 
        (into [] (map #(count (control-key %)) video))]

    ;;Now grab from list using the following indicies:
    ;;     start: sum of all sizes before current block 
    ;;     end: start + current block size
    (map-indexed
     (fn [index cur-size]
       (let [start-index (reduce + 0 (subvec list-of-sizes 0 index))]
         (subvec
          l
          start-index
          (+ start-index cur-size))))
     list-of-sizes)))



(defn assoc-viewable-words
  "Adds :viewable-words to block which adds pre and post blocks to the words"
  [caption-blocks]
  (mapv
   (fn [pre cur post]
     (assoc cur 
            ::viewable-words

            (str (get pre ::words "")
                 " "
                 (get cur ::words)
                 " "
                 (get post ::words ""))))
   (into [] (conj (drop-last caption-blocks) nil))
   caption-blocks
   (conj (into [] (rest caption-blocks)) nil)))

(defn load-caption [f]
  (let [caption-blocks (load-caption-blocks f)]
   {::blocks (assoc-viewable-words caption-blocks)
    ::full-caption (de-blockify-words caption-blocks)}))

(defn add-pos [video]
  (map
   #(assoc %1 :pos %2)
   video
   (blockify-list
    (nlp-utils/tag-pos (de-blockify video ::words))
    video
    ::words)))

(def into-string-array (partial into-array String))

(defn add-lemmas
  "Assocs lemmas into the video map"
  [video]
  (if (nil? ((first video) :pos))
    (throw (Exception. "Video needs parts of speech to lemmatize")))
  (-> (into [] (apply nlp-utils/lemmatize
                   [(-> video
                        (de-blockify ::words)
                        into-string-array)
                    (-> video
                        (de-blockify :pos)
                        into-string-array)]))
      (blockify-list video ::words)
      ((partial
        map
        (fn [block lemmas] (assoc block :lemmas lemmas))
        video))))


(def srt-source-folders (config/all :srt-source-folders))

(defn get-all-subtitle-file-objs [srt-source-folders]
  (flatten
   (map
    (fn [subtitle-folder]
      (->> (clojure.java.io/file subtitle-folder)
          file-seq
          (filter #(.isFile %))
          (map (fn [file] {:caption-file file}))))
    srt-source-folders)))


(def all-subtitle-files (get-all-subtitle-file-objs srt-source-folders))

(defn video-ids [subtitle-files]
  (->> subtitle-files
       (map #(get % :caption-file))
       (map #(.getName %))
       (map #(subs % 0 11))
       (map #(assoc %1 ::id %2) subtitle-files)))

(defn get-video-attrs [videos]

  (->> (reduce #(str %1 "," (::id %2))
               (::id (first videos))
               (rest videos))

       youtube/video-data
       :body
       (#(json/decode % true))
       :items
       (map (fn [video

                 {{description :description
                   uploaded :publishedAt
                   title :title
                   channel-title :channelTitle
                   {thumbnail :standard alt-thumbnail :default} :thumbnails}
                  :snippet

                  {views :viewCount
                   likes :likeCount}
                  :statistics}]
              (assoc video 
                     ::description description
                     ::uploaded uploaded
                     ::title title
                     ::channel-title channel-title
                     ::views (parse-int views)
                     ::likes (parse-int likes)
                     ::thumbnail (if-let [url (:url thumbnail)]
                                   url
                                   (:url alt-thumbnail))))
            videos)))

(s/def ::id            string?)
(s/def ::channel-title string?)
(s/def ::description   string?)
(s/def ::full-caption  string?)
(s/def ::caption (s/keys :req [::blocks ::full-caption]))

(s/def ::video (s/keys :req [::id
                             ::channel-title
                             ::description
                             ::caption
                             ::title]))

(s/def ::videos (s/coll-of ::video))

(defn make-index-struct
  [video-struct]
  {:id (video-struct ::id)
   :title_t  (video-struct ::title)
   :description_t (video-struct ::description)
   :uploaded_dt (video-struct ::uploaded)
   :likes_i (video-struct ::likes)
   :views_i (video-struct ::views)
   :channel_title_s (video-struct ::channel-title)
   :captions_t (get-in video-struct [::caption ::full-caption])
   :thumbnail_s (video-struct ::thumbnail)})


(defn make-index-block-struct
  [video-struct]
  (mapv
   (fn [block]
     {:id (str (video-struct ::id) "-" (block ::block-id)) 
      :video_id_s (video-struct ::id)
      :captions_t (block ::words)
      :viewable_words_t (block ::viewable-words)
      :start_time_s (time-struct->string (get-in block [::time :start]))
      :stop_time_s (time-struct->string (get-in block [::time :stop]))})
   (get-in video-struct [::caption ::blocks])))

(def connection (config/all :solr-connection))
(def block-connection (config/all :solr-block-connection))

(defn index [video-structs]
  (->>
   (doall
    (mapv
     make-index-struct
     video-structs))
   (#(println (solr/add-docs connection %))))
  ;video-structs
  )

(defn index-blocks [video-structs]
  (->>
   (flatten
    (doall
     (mapv
      make-index-block-struct
      video-structs)))
   (partition 500)
   (map #(println (solr/add-docs block-connection %)))))

(defn index-all-videos []
  (doall
   (->> srt-source-folders
        get-all-subtitle-file-objs
        video-ids
        (map #(assoc % ::caption (load-caption (:caption-file %))))
        (partition 10)
        (map #(get-video-attrs %))
        ;;(map #(map make-index-block-struct %))
        ;;(map #(map make-index-struct %))
        (map #(index %))
        ;;(map #(index-blocks %))`
        )))

(defn index-all-video-blocks []
   (->> srt-source-folders
        get-all-subtitle-file-objs
        video-ids
        (map #(assoc % ::caption (load-caption (:caption-file %))))
        (partition 10)
        (map #(get-video-attrs %))
        ;;(mapv #(mapv make-index-block-struct %))
        ;;(map #(map make-index-struct %))
        ;(map #(index %))
        (map #(index-blocks %))
        ))

