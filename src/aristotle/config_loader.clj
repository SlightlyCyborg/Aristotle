(ns aristotle.config-loader
  (:require [clojure.edn :as edn]))



(defn reload []
  (def all (edn/read-string (slurp "resources/config.edn"))))

(reload)
