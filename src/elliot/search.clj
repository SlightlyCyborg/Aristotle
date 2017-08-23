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
           :hl "on"
           :hl-fl "viewable_words_t"}))


(defn get-blocks [query-struct doc]
  (let [query-response (query-solr-for-blocks query-struct doc)]
   (assoc doc
          :highlight-html
          (map
           (fn [result]
             (first
              (:viewable_words_t
               (get (:highlights query-response)
                    (keyword (:id result))))))
           (:docs query-response)))))

(defn go [query-struct]
  (let [rv (mapply solr/query connection query-struct)]
    (assoc
     rv
     :docs
     (map
      (partial get-blocks query-struct)
      (:docs rv)))))