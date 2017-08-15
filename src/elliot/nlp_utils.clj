(ns elliot.nlp-utils
  (:import [opennlp.tools.tokenize WhitespaceTokenizer]
           [opennlp.tools.postag POSTaggerME POSModel])
  (:require [clojure.spec.alpha :as s]))


(def tokenizer WhitespaceTokenizer/INSTANCE)
(defn tokenize [str]
  (vec (.tokenize tokenizer str)))



(def pos-tagger-model-location "resources/en-pos-maxent.bin")
(def pos-tagger-model
  (POSModel. (java.io.FileInputStream. pos-tagger-model-location)))
(def pos-tagger (POSTaggerME. pos-tagger-model))

(s/def ::words (s/coll-of string?))

(defn tag-pos
  "Returns a list of parts of speech that coresponds with the whole-transcript list"
  [words]
  (into []
        (.tag pos-tagger
              (into-array words))))



