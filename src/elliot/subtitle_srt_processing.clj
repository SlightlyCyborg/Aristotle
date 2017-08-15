(ns elliot.subtitle-srt-processing
  (:use clojure.test)
  (:require [clojure.spec.alpha :as s])
  (:import [opennlp.tools.tokenize WhitespaceTokenizer]
           [opennlp.tools.postag POSTaggerME POSModel]))

(def subtitle-folder "resources/subs/elliot_hulse/")

(defn get-available-subtitle-files []
  (-> (clojure.java.io/file subtitle-folder)
      file-seq
      ((partial filter #(.isFile %)))))


(s/def ::block-id int?)

(s/def ::single-video-caption (s/keys :req [::block-id ::words]))

(def tokenizer WhitespaceTokenizer/INSTANCE)
(defn tokenize [str]
  (vec (.tokenize tokenizer str)))

(defn parse-int [s]
   (Integer. (re-find  #"\d+" s )))

(defn parse-srt [f]
  (-> (slurp f)
      (clojure.string/replace #"<[^>]+>" "")
      (clojure.string/split #"\r\n\r\n")
      ((partial map #(clojure.string/split % #"\r\n")))
      ((partial map #(do {:block-id (parse-int (first %))
                          :words (tokenize (nth % 2))})))))

(defn get-whole-transcript [blocks]
    (reduce
     #(concat %1 (:words %2))
     [] 
     blocks))

(s/def ::whole-transcript (s/coll-of string?))

(s/fdef get-pos-for-whole-transcript
        :args ::whole-transcript)

(def pos-tagger-model-location "resources/en-pos-maxent.bin")
(def pos-tagger-model
  (POSModel. (java.io.FileInputStream. pos-tagger-model-location)))
(def pos-tagger (POSTaggerME. pos-tagger-model))

(defn get-pos-for-whole-transcript
  "Returns a list of parts of speech that coresponds with the whole-transcript list"
  [whole-transcript]
  (into []
        (.tag pos-tagger
              (into-array whole-transcript))))




 (defn blockify
   "Divides a list up into blocks of the same length as the control data structure. If total item count of list & control data structure do not equal, then an error is thrown"
   [l control]
   (if (not (= (count (get-whole-transcript control)) (count l)))
     (throw (Exception. "The list is not same size as control words")))

   (let [l (vec l) ;;Need data to be in vec for subvec

         list-of-sizes ;;Build up the list-of-sizes 
         (into [] (map #(count (:words %)) control))]

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


  ;;Tests
(deftest test-blockify
  (let [control
        (parse-srt "resources/subs/elliot_hulse/p2zvOJe1Iq4_0_en.srt")]

   (is (thrown? Exception
                (blockify
                 (cons :foo (get-whole-transcript control))
                 control)))
   (let [blocked (blockify (get-whole-transcript control) control)]
    (dotimes [x 4]
      (let [rand-ind (rand-int (count control))]
        (is (= (into [] (:words (nth control rand-ind)))
               (nth blocked rand-ind))))))))



(defn load-all-subtitles []
  (-> (get-available-subtitle-files)
      ((partial map #(parse-srt %)))))

(defn collect-all-words [subs]
  (reduce
   #(apply conj %1
           (reduce
            (fn [col block] (apply conj col (:words block)))
            []
            %2))
   []
   subs))



