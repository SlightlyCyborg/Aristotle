(ns elliot.search
  (:require [clj-solr.core :as solr]
            [clojure.spec.alpha :as s]
            [elliot.config-loader :as config]))

(def connection (:solr-connection config/all))

(defn mapply [f & args]
  (apply f (apply concat (butlast args) (last args))))

(defn go [query-struct]
  (mapply solr/query connection query-struct))
