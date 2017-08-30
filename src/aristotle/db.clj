(ns aristotle.db
  (:use korma.db
        korma.core))

(defdb db (sqlite3 {:db "aristotle.db"}))

(defentity videos)

(select videos)
