(ns elliot.db
  (:use korma.db
        korma.core))

(defdb db (sqlite3 {:db "elliot.db"}))

(defentity videos)

(select videos)
