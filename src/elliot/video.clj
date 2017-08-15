(ns elliot.video
  (:use clojure.test)
  (:require [clojure.spec.alpha :as s]
            [elliot.nlp-utils :as nlp-utils])
  (:import [opennlp.tools.tokenize WhitespaceTokenizer]
           [opennlp.tools.postag POSTaggerME POSModel]))

(s/def ::words (s/coll-of string?))
(s/def ::block-id int?)

(s/def ::block (s/keys :req [::block-id ::words]))
(s/def ::blocks (s/coll-of ::block))

(s/def ::video (s/keys :req [::blocks]))

(defn parse-int [s]
   (Integer. (re-find  #"\d+" s )))

(defn make-video [f]
  (-> (slurp f)
      (clojure.string/replace #"<[^>]+>" "")
      (clojure.string/split #"\r\n\r\n")
      ((partial map #(clojure.string/split % #"\r\n")))
      ((partial map #(do {:block-id (parse-int (first %))
                          :words (nlp-utils/tokenize (nth % 2))})))))

(defn de-blockify [video key]
  (reduce
   #(concat %1 (key %2))
   [] 
   video))


(defn blockify-list
   "Divides a list up into blocks of the same length as the control data structure. If total item count of list & control data structure do not equal, then an error is thrown"
  [l video control-key]
  (if (not (= (count (de-blockify video :words)) (count l)))
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


  ;;Tests
(deftest test-blockify
  (let [control
        (make-video "resources/subs/elliot_hulse/p2zvOJe1Iq4_0_en.srt")]

   (is (thrown? Exception
                (blockify-list
                 (cons :foo (de-blockify control :words))
                 control
                 :words)))
   (let [blocked (blockify-list (de-blockify control :words)
                           control
                           :words)]
    (dotimes [x 4]
      (let [rand-ind (rand-int (count control))]
        (is (= (into [] (:words (nth control rand-ind)))
               (nth blocked rand-ind))))))))



