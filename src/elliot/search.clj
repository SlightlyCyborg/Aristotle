(ns elliot.search
  (:require [clj-solr.core :as solr]
            [clojure.spec.alpha :as s]
            [elliot.config-loader :as config]))

(def connection (:solr-connection config/all))
(def block-connection (:solr-block-connection config/all))

(defn mapply [f & args]
  (apply f (apply concat (butlast args) (last args))))

(defn process-highlights [response]
  (println (response :highlights))
  response)

(defn query-solr-for-blocks [query-struct doc]
  (mapply solr/query block-connection
          {:q (str "captions_t:"
                   (:q query-struct)
                   " AND video_id_s:\""
                   (doc :id)
                   "\"")
           :sort "start_time_s asc"
           :hl "on"
           :hl-fl "viewable_words_t"}))


(defn get-blocks [query-struct doc]
  (let [query-response (query-solr-for-blocks query-struct doc)]
   (assoc doc
          :blocks
          (map
           (fn [result]
             {:highlight
              (first
               (:viewable_words_t
                (get (:highlights query-response)
                     (keyword (:id result)))))

              :start-time (:start_time_s result)
              :stop-time (:stop_time_s result)})
           (:docs query-response)))))

(defn go [query-struct]
  (let [rv (mapply solr/query connection (assoc query-struct
                                                :q
                                                (str
                                                 "title_t:\""
                                                 (:q query-struct)
                                                 "\""
                                                 (:q query-struct))))]
    (assoc
     rv
     :docs
     (map
      (partial get-blocks query-struct)
      (:docs rv)))))
