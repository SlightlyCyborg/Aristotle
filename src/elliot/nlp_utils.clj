(ns elliot.nlp-utils
  (:import [opennlp.tools.tokenize WhitespaceTokenizer]
           [opennlp.tools.postag POSTaggerME POSModel]
           [opennlp.tools.lemmatizer DictionaryLemmatizer]
           [edu.stanford.nlp.pipeline.*])
  (:require [clojure.spec.alpha :as s]))


;;Tokenizer

(def tokenizer WhitespaceTokenizer/INSTANCE)
(defn tokenize [str]
  (vec (.tokenize tokenizer str)))


;;Part of Speech

(def pos-tagger-model-location "resources/en-pos-perceptron-500-dev.bin" )
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

(def lemmatizer-dict-location "resources/en-lemmatizer.dict")

;; Lemmatizer
(def lemmatizer
  (DictionaryLemmatizer.
   (java.io.FileInputStream. lemmatizer-dict-location)))

(defn lemmatize [words poss]
  (.lemmatize lemmatizer words poss))

