(ns elliot.config-loader
  (:require [clojure.edn :as edn]))


(def all (edn/read-string (slurp "resources/config.edn")))

(def srt-source-folders (all :srt-source-folders))


