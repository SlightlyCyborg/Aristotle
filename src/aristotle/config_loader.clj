(ns aristotle.config-loader
  (:require [clojure.edn :as edn]))


(defn get-all-config-names []
  (->>
   (clojure.java.io/file "resources/configs")
   .list
   (into [])))

(defn gen-config-fname [daemon-name]
  (str "resources/configs/" daemon-name "/config.edn"))

(defn load-config [daemon-name]
  (println (str "had to slurp file: " daemon-name))
  (edn/read-string
   (slurp (gen-config-fname daemon-name))))

(defn get-last-modified [daemon-name]
  (.lastModified (clojure.java.io/file
                  (gen-config-fname daemon-name))))

(def all (atom {}))

(defn get [daemon-name]
  (let [daemon-name (name daemon-name)]
   (swap! all (fn [last-configs]
                (merge
                 last-configs
                 (let [last-modified (get-last-modified daemon-name)
                       previous-last-modified (get-in
                                               last-configs 
                                               [(keyword daemon-name)
                                                :last-modified])]

                   (if (or (nil? previous-last-modified)
                           (> last-modified previous-last-modified))
                     {(keyword daemon-name)
                      (assoc (load-config daemon-name)
                             :last-modified last-modified)}
                     nil)))))))

