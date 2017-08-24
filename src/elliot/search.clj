(ns elliot.search
  (:require [clj-solr.core :as solr]
            [clojure.spec.alpha :as s]
            [elliot.config-loader :as config]
            [elliot.common-fns :as common]))

(def connection (:solr-connection config/all))
(def block-connection (:solr-block-connection config/all))
(def field-to-display-and-highlight :viewable_words_t)

(defn query-solr-for-blocks
  "Takes a query struct and a doc and then searches video blocks for the blocks in which the query occurs in the particular video"
  [query-struct video-doc]
  (common/mapply
   solr/query block-connection
   {:q (str "captions_t:"
            (:q query-struct)
            " AND video_id_s:\""
            (video-doc :id)
            "\"")
    :sort "start_time_s asc"
    :hl "on"
    :hl-fl (name field-to-display-and-highlight)}))


(defn extract-data-from-block-query-response
  [highlights block-query-response]
  {:highlight
   (first
    (field-to-display-and-highlight
     (get highlights 
          (keyword (:id block-query-response)))))

   :start-time (:start_time_s block-query-response)
   :stop-time (:stop_time_s block-query-response)})

(defn get-blocks [query-struct doc]
  (let [query-response (query-solr-for-blocks query-struct doc)]
   (assoc doc
          :blocks
          (map
           (partial extract-data-from-block-query-response
                    (:highlights query-response))
           (:docs query-response)))))

(defn improve-query [stock-query]
  (assoc stock-query
         :q
         (str
          "title_t:\""
          (:q stock-query)
          "\""
          (:q stock-query))))

(defn go [query-struct]
  (if (nil? (query-struct :q))
    ;;Don't bother querying, it is an empty query
    {:docs []}

    ;;Do an actual query
    (let [rv (common/mapply
              solr/query connection (improve-query query-struct))]
      (assoc
       rv
       :docs
       (map
        (partial get-blocks query-struct)
        (:docs rv))))))
