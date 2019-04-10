(ns aristotle.search
  (:require [clj-solr.core :as solr]
            [clojure.spec.alpha :as s]
            [aristotle.config-loader :as config]
            [aristotle.common-fns :as common]))

(def connection (:solr-connection config/all))
(def block-connection (:solr-block-connection config/all))
(def field-to-display-and-highlight :viewable_words_t)


(defn get-solr-connection [daemon-name]
  (get (config/get-by-name daemon-name) :solr-connection))

(defn get-solr-block-connection [daemon-name]
  (get (config/get-by-name daemon-name) :solr-block-connection))

(defn query-solr-for-blocks
  "Takes a query struct and a doc and then searches video blocks for the blocks in which the query occurs in the particular video"
  [daemon-name query-struct video-doc]
  (common/mapply
   solr/query (get-solr-block-connection daemon-name)
   {:q (str "captions_t:\""
            (:q query-struct)
            "\""
            " AND video_id_s:\""
            (video-doc :id)
            "\"")
    :sort "start_time_s asc"
    :hl "on"
    :hl-fl (name field-to-display-and-highlight)}))


(defn extract-data-from-block-query-response
  [highlights block-query-response]
  ;;(println (get highlights (keyword (:id block-query-response))))
  {:highlight
   (first
    (field-to-display-and-highlight
     (get highlights 
          (keyword (:id block-query-response)))))

   :start-time (:start_time_s block-query-response)
   :stop-time (:stop_time_s block-query-response)})

(defn get-blocks [query-struct daemon-name doc]
  (let [query-response (query-solr-for-blocks daemon-name query-struct doc)]
   (assoc doc
          :blocks
          (map
           (partial extract-data-from-block-query-response
                    (:highlights query-response))
           (:docs query-response)))))

(defn print-n-return [thing]
  (println thing)
  thing)

(defn improve-query [stock-query]
  (assoc stock-query
         :q
         (print-n-return
          (str
           "title_t:\""
           (:q stock-query)
           "\"^10 "
           "captions_t:\""
           (:q stock-query)
           "\""

           " \""
           (:q stock-query)
           "\"~10000^5"
           ))))

(defn go [query-struct daemon-name]
  (if (nil? (query-struct :q))
    ;;Don't bother querying, it is an empty query
    {:docs []}

    ;;Do an actual query
    (let [rv (common/mapply
              solr/query
              (get-solr-connection daemon-name)
              (improve-query query-struct))]
      (assoc
       rv
       :docs
       (map
        (partial get-blocks query-struct daemon-name)
        (:docs rv))
       :num-found
       (:num-found rv)))))
