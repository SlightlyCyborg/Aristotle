(ns elliot.blog 
  (:require [pegasus.core :refer [crawl]]
            [pegasus.dsl :refer :all])
  (:import (java.io StringReader)))



(def blog-url "http://elliotthulse.com/blog/")

(defn crawl-blog []
  (crawl {:seeds [blog-url]
          :user-agent "Pegasus web crawler"
          :corpus-size 7000 ;; crawl 20 documents
          :job-dir "/tmp/elliot-blog-all"
          :extractor
          (defextractors
            (extract :at-selector [:.entry-title :a]
                     :follow :href)

            (extract :at-selector [:.x-pagination :a]

                     :follow :href))}))


(defn load [crawled-corpus-location]
  (let [blog-struct nil]
    blog-struct))


(defn index [blog-structs])
