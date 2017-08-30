(ns aristotle.data
  (:require [aristotle.videos :as videos]
            [clojure.pprint :refer [pprint]]))


(defn collect-all-words-into-a-set [all-words]
  (set all-words))


(def all-subtitles (videos/load-all-videos))
(def all-words (subs-proc/collect-all-words all-subtitles))
(def unique-words (collect-all-words-into-a-set all-words))

(def aristotle-analytics
  {:total-number-of-words-on-camera (count all-words)
   :vocabulary-size (count unique-words)})



