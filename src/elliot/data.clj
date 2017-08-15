(ns elliot.data
  (:require [elliot.subtitle-srt-processing :as subs-proc])
  (:import [opennlp.tools.lemmatizer DictionaryLemmatizer]))


(def lemmatizer-dict "resources/en-lemmatizer.dict")
(def lemmatizer (DictionaryLemmatizer.
                 (java.io.FileInputStream. lemmatizer-dict)))

(defn lemmatize [words]
  (-> (make-array String/TYPE words)
      ((partial .lemmatize lemmatizer))
      ))

(defn collect-all-words-into-a-set [all-words]
  (set all-words))


(def all-subtitles (subs-proc/load-all-subtitles))
(def all-words (subs-proc/collect-all-words all-subtitles))
(def unique-words (collect-all-words-into-a-set all-words))

(def elliot-analytics
  {:total-number-of-words-on-camera (count all-words)
   :vocabulary-size (count unique-words)})



