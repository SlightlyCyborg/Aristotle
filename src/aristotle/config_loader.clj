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
  (try
    (edn/read-string
     (slurp (gen-config-fname daemon-name)))
    (catch Exception e {:not-found true})))

(defn get-last-modified [daemon-name]
  (.lastModified (clojure.java.io/file
                  (gen-config-fname daemon-name))))

(def all (atom {}))

(defn modified? [last-configs daemon-name]
  (let [last-modified (get-last-modified daemon-name)
        previous-last-modified (get-in
                                last-configs 
                                [(keyword daemon-name)
                                 :last-modified])]
      (or (nil? previous-last-modified)
       (> last-modified previous-last-modified))))

(def server (edn/read-string (slurp "resources/config.edn")))

(defn get-by-name [daemon-name]
  (let [daemon-name (name daemon-name)]
    ((keyword daemon-name)
     (swap! all (fn [last-configs]
                  (merge
                   last-configs
                   (let [last-modified (get-last-modified daemon-name)]
                     (if (modified? last-configs daemon-name)
                       {(keyword daemon-name)
                        (assoc (load-config daemon-name)
                               :last-modified last-modified)}
                       nil))))))))
